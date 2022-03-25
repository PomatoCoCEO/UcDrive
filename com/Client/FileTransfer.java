package com.Client;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import com.Client.conn.ClientConnection;

public class FileTransfer extends Thread {
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private int byteSize, noBlocks;
    public FileTransfer (ObjectInputStream ois, ObjectOutputStream oos, int byteSize, int noBlocks) {
        this.ois = ois;
        this.oos = oos;
        this.byteSize = byteSize;
        this.noBlocks = noBlocks;
    }

    public static void downloadFile(String name) {
        
    } 

    public void run() {

    }

}