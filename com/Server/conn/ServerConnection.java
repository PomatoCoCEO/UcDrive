package com.Server.conn;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

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
        this.socket = socket;
        this.auth = server.getAuthInfo();
        this.absolutePath = server.getAbsolutePath();
        this.server=server;
        out = new ObjectOutputStream(socket.getOutputStream());
        out.flush();
        in = new ObjectInputStream(socket.getInputStream());
        this.start();
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

        commandHandler.login(loginRequest);
        System.out.println("Authentication successful! Let us continue!");
        while (true)

        {
            try {
                // handle request
                Request request = (Request) in.readObject();
                System.out.println("Request: \"" + request + "\"");
                commandHandler.handleRequest(request);
                // and send response
            } catch (EOFException e) {
                System.out.println("EOF:" + e);
                return;
            } catch (IOException e) {
                e.printStackTrace();
                return;
            } catch (ClassNotFoundException cnf) {
                cnf.printStackTrace();
                return;
            } catch (AuthorizationException ae) {
                ae.printStackTrace();
            }

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

    public void sendReply(Reply reply) {
        try {
            out.writeObject(reply);
            out.flush();
        } catch (IOException io) {
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
