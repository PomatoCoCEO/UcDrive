package com.Client;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;

import com.Client.conn.ClientConnection;
import com.DataTransfer.FileChunk;

public class FileTransfer extends Thread {
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private int byteSize, noBlocks;
    private String fileName;
    public FileTransfer (ObjectInputStream ois, ObjectOutputStream oos, int byteSize, int noBlocks, String fileName) {
        this.ois = ois;
        this.oos = oos;
        this.byteSize = byteSize;
        this.noBlocks = noBlocks;
        this.fileName = fileName;
        this.start();
    }

    public void run() {
        try {
            String fileNameWithoutDirectory = fileName.substring(fileName.lastIndexOf('/'));
            String filePath = Paths.get(Client.getClientDir(),fileNameWithoutDirectory).toString();  
            File myObj = new File(filePath);
            FileOutputStream fos = new FileOutputStream(myObj);
            if (myObj.createNewFile()) {
                System.out.println("File created: " + myObj.getName());
            } else {
                System.out.println("File already exists.");
            }
            for(int i = 0; i< noBlocks; i++){
                FileChunk chunk = (FileChunk) ois.readObject();
                // ! we might need to cache this
                fos.write(chunk.getBytes());
                fos.flush();
            }
            fos.close();
            ois.close();
            oos.close();
        } catch (IOException e) {
            System.out.println("An error occurred:");
            e.printStackTrace();
        } catch(ClassNotFoundException cnf) {
            System.out.println("An error occurred:");
            cnf.printStackTrace();
        }
    }

}