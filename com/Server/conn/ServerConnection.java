package com.Server.conn;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Path;

import com.Server.CommandHandler;
import com.Server.Server;
import com.Server.auth.*;
import com.Server.except.AuthorizationException;
import com.DataTransfer.Reply;
import com.DataTransfer.Request;

//

public class ServerConnection extends Thread {
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private Auth auth;
    private User user;
    private Server server;
    private String absolutePath;

    public ServerConnection(Socket socket, Server server) throws IOException {

        System.out.println("New server connection");
        this.socket = socket;
        this.auth = server.getAuthInfo();
        this.absolutePath = server.getAbsolutePath();
        this.server = server;
        out = new ObjectOutputStream(socket.getOutputStream());
        out.flush();
        in = new ObjectInputStream(socket.getInputStream());
        // this.start();
    }

    public void run() {
        CommandHandler commandHandler = new CommandHandler(this.socket, this);

        Request loginRequest;
        try {
            loginRequest = (Request) in.readObject();
        } catch (ClassNotFoundException | IOException e1) {
            e1.printStackTrace();
            return;
        }
        try {
            commandHandler.login(loginRequest);
        } catch(SocketException se) {
            se.printStackTrace();
            return;
        }

        while (true)

        {
            try {
                // handle request
                if (this.socket != null) {

                    Request request = (Request) in.readObject();
                    System.out.println("Request: \"" + request + "\"");
                    commandHandler.handleRequest(request);
                } else {
                    break;
                }
                // and send response
            } catch (EOFException e) {
                this.getUser().setToken("");

                System.out.println("EOF:" + e);
                try {
                    this.socket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                return;
            } catch (IOException e) {
                this.getUser().setToken("");

                e.printStackTrace();
                return;
            } catch (ClassNotFoundException cnf) {
                this.getUser().setToken("");

                cnf.printStackTrace();
                return;
            } catch (AuthorizationException ae) {
                this.getUser().setToken("");
                ae.printStackTrace();
            }

        }
    }

    public void close() {
        try {
            if (!socket.isClosed()) {
                this.socket.close();
                this.socket = null;
            }
        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    public Auth getAuth() {
        return auth;
    }

    public User getUser() {
        return user;
    }

    public User setUser(User user) {
        return this.user = user;
    }

    public String getAbsolutePath() {
        return this.absolutePath;
    }

    public void sendReply(Reply reply) throws SocketException {
        try {
            out.writeObject(reply);
            out.flush();
        } catch(SocketException se) {
            throw se;
        }

        catch (IOException io) {
            io.printStackTrace();
        }
    }

    public void constructAndSendReply(String message, String statusCode) {
        Reply reply = new Reply(message, statusCode);
        try {
            out.writeObject(reply);
            out.flush();
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    public Request getRequest() {
        Request req = new Request("", "");
        try {
            req = (Request) in.readObject();
        } catch (ClassNotFoundException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return req;
    }

    public Server getServer() {
        return server;
    }
}
