package com.Server.conn;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import com.Server.auth.*;
import com.DataTransfer.Reply;
import com.DataTransfer.Request;

//

public class ServerConnection extends Thread {
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private Auth auth;

    public ServerConnection(Socket socket, Auth auth) throws IOException {
        this.socket = socket;
        this.auth = auth;
        in = new ObjectInputStream(socket.getInputStream());
        out = new ObjectOutputStream(socket.getOutputStream());
        this.start();
    }

    public void run() {
        String response;
        Request request;
        Reply reply;
        boolean authenticated = false;
        while (!authenticated) {
            try {
                request = (Request) in.readObject();
                String[] sp = request.getMessage().split(" ");
                auth.authenticate(sp[2], sp[4]);
                authenticated = true;
                reply = new Reply(sp[2], "OK");
                out.writeObject(reply);
            } catch (EOFException e) {
                System.out.println("EOF:" + e);
                return;
            } catch (Exception e) {

                System.err.println("No authentication was possible");
                reply = new Reply("Login unsuccessful", "Unauthorized");
                try {
                    out.writeObject(reply);
                } catch (IOException io) {
                    io.printStackTrace();
                }
                return;
                //
            }
            // determine a suitable token
        }
        System.out.println("Authentication successful! Let us continue!");
        while (true)

        {
            try {
                // handle request
                request = (Request) in.readObject();
                System.out.println("Request: \"" + request + "\"");
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
            }
        }
    }
}
