package com.Client;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import com.Client.conn.ClientConnection;

public class FileTransfer extends Thread {
    private Socket socket;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    public FileTransfer (Socket socket) {
        this.socket=socket;
        try {
            this.ois = new ObjectInputStream (socket.getInputStream());
            this.oos = new ObjectOutputStream (socket.getOutputStream());
        } catch(IOException io) {
            io.printStackTrace();
        }
    }

    public static void downloadFile(String name) {
        
    } 

    public void run() {

    }

}