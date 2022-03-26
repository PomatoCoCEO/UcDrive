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
    private long byteSize, noBlocks;
    private String fileName;

    public FileTransfer(ObjectInputStream ois, ObjectOutputStream oos, long byteSize, long noBlocks, String fileName) {
        this.ois = ois;
        this.oos = oos;
        this.byteSize = byteSize;
        this.noBlocks = noBlocks;
        this.fileName = fileName;
        this.start();
    }

    public void run() {
        try {
            String fileNameWithoutDirectory = fileName.substring(fileName.lastIndexOf('/')+1);
            String filePath = Paths.get(Client.getClientDir(), fileNameWithoutDirectory).toString();
            File myObj = new File(filePath);
            if (myObj.createNewFile()) {
                System.out.println("File created: " + myObj.getName());
            } else {

                // ! give the file another name maybe

                System.out.println("File already exists.");
            }
            FileOutputStream fos = new FileOutputStream(myObj);
            for (int i = 0; i < noBlocks; i++) {

                // ! check last block size ??

                FileChunk chunk = (FileChunk) ois.readObject();
                // ! we might need to cache this
                fos.write(chunk.getBytes());
                fos.flush();
            }
            fos.close();
            ois.close();
            oos.close();
            System.out.println(fileName+" : transfer complete");
        } catch (IOException e) {
            System.out.println("An error occurred:");
            e.printStackTrace();
        } catch (ClassNotFoundException cnf) {
            System.out.println("An error occurred:");
            cnf.printStackTrace();
        }
    }

}