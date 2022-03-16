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
    private User user;

    public ServerConnection(Socket socket, Auth auth) throws IOException {
        this.socket = socket;
        this.auth = auth;
        in = new ObjectInputStream(socket.getInputStream());
        out = new ObjectOutputStream(socket.getOutputStream());
        this.start();
    } 

    public boolean login() {
        String response;
        Request request;
        Reply reply;
        boolean authenticated = false;
        while (!authenticated) {
            try {
                request = (Request) in.readObject();
                /* this is a simple way to ensure the message is read correctly.
                 * \n's cannot be read in the console
                 */
                String[] sp = request.getMessage().split("\n"); 
                if(!sp[0].equals("LOGIN")) {
                    throw new Exception("Invalid command")
                }
                this.user = auth.authenticate(sp[1], sp[2]);
                authenticated = true;
                reply = new Reply(sp[1]+"\n"+this.user.getLastDir(), "OK"); 
                // sends a token (to be implemented) and the last working directory
                out.writeObject(reply);
                return true;
            } catch (EOFException e) { // happens when socket is closed on the other side
                System.out.println("EOF:" + e);
                return false;
            } catch (Exception e) {

                System.err.println("No authentication was possible");
                reply = new Reply("Login unsuccessful", "Unauthorized");
                try {
                    out.writeObject(reply);
                } catch (IOException io) {
                    io.printStackTrace();
                }
            }
            // determine a suitable token
        }
        return true;    
    }

    
    public void run() {
        
        
        if(!login()) return;    

        System.out.println("Authentication successful! Let us continue!");
        while (true)

        {
            try {
                // handle request
                Request request = (Request) in.readObject();
                System.out.println("Request: \"" + request + "\"");
                if(!request.getToken().equals(this.user.getToken())) {
                    //! bad things are happening
                    reply = new Reply("Wrong authentication", "Unauthorized");
                    out.writeObject(reply);
                    throw new Exception("Invalid auth token for user "+user.)
                }
                handleRequest(request);
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

    public Auth getAuth() {
        return auth;
    }

    public 
}
