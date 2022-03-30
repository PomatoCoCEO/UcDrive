package com.Client.conn;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import com.Client.Client;
import com.Client.config.ConfigClient;
import com.DataTransfer.Reply;
import com.DataTransfer.Request;

public class ClientConnection {

    private ObjectOutputStream out;
    private ObjectInputStream in;
    private ConfigClient config;
    private Socket socket;

    public ClientConnection(){}

    public ClientConnection(Socket socket, ConfigClient config) throws IOException {
        this.socket = socket;
        this.config = config;
        out = new ObjectOutputStream(socket.getOutputStream());
        out.flush();
        in = new ObjectInputStream(socket.getInputStream());

    }

    public void sendRequest(Request request) throws SocketException {
        try {
            out.writeObject(request);
            out.flush();
        } catch(SocketException e) {
            throw e;
        }catch (IOException io) {
            io.printStackTrace();
        }
    }

    public void constructAndSendRequest(String message, String token) {
        Request req = new Request(message,token);
        try {
            out.writeObject(req);
            out.flush();
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    // ! to change
    public void constructAndSendReply(String message, String statusCode) {
        Reply reply = new Reply(message, statusCode);
        try {
            out.writeObject(reply);
            out.flush();
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    public Reply getReply() throws SocketTimeoutException, SocketException {
        Reply reply = new Reply("", "");
        try {
            reply = (Reply) in.readObject();
        } catch(SocketTimeoutException | SocketException e) {
            throw e;
        } catch (ClassNotFoundException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return reply;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocketParams(Socket sock, ObjectInputStream oisNew, ObjectOutputStream oosNew) {
        try {
            if(this.socket!= null){
                this.socket.close();
            }
            this.socket = sock;
            this.out = oosNew;
            this.in = oisNew;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } 
    }

}