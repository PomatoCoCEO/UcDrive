package com.Client.conn;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import com.DataTransfer.Reply;
import com.DataTransfer.Request;

public class ClientConnection {

    private static ObjectOutputStream out;
    private static ObjectInputStream in;
    private Socket socket;

    public ClientConnection(Socket socket) throws IOException {
        this.socket = socket;
        out = new ObjectOutputStream(socket.getOutputStream());
        out.flush();
        in = new ObjectInputStream(socket.getInputStream());

    }

    public void sendRequest(Request request) {
        try {
            out.writeObject(request);
            out.flush();
        } catch (IOException io) {
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

    public Reply getReply() {
        Reply reply = new Reply("", "");
        try {
            reply = (Reply) in.readObject();
        } catch (ClassNotFoundException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return reply;
    }

    public Socket getSocket() {
        return socket;
    }

}