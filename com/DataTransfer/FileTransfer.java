package com.DataTransfer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Paths;

import com.Client.Client;

public class FileTransfer extends Thread {
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private long byteSize, noBlocks;
    private String dirPath, fileName;
    public static final int BLOCK_BYTE_SIZE = 8192;
    private boolean send;

    public FileTransfer (ObjectInputStream ois, ObjectOutputStream oos, long byteSize, long noBlocks, String dirPath, String fileName, boolean send) {
        this.ois = ois;
        this.oos = oos;
        this.byteSize = byteSize;
        this.noBlocks = noBlocks;
        this.dirPath = dirPath;
        this.fileName = fileName;
        this.send=send;
        this.start();
    }

    public void run() {
        if(send) sendFile();
        else receiveFile();
    }
    private void sendFile() {
        try {
            // String fileName = absolutePath.substring(absolutePath.lastIndexOf("/")+1); // for linux and mac
            // if(fileName.indexOf("\\")!=-1) fileName = absolutePath.substring(absolutePath.lastIndexOf("\\")+1); // for windows
            Reply rep = new Reply("FILE\n" + fileName +"\nSIZE\n"+byteSize+"\nBLOCKS\n"+noBlocks, "OK");
            oos.writeObject(rep);
            oos.flush();
            FileInputStream fis = new FileInputStream(new File(Paths.get(dirPath,fileName).toString()));
            byte [] toSend = new byte[BLOCK_BYTE_SIZE];
            for(int i = 0; i< noBlocks; i++) {
                int bytesToSend = fis.read(toSend);
                
                FileChunk fc;
                if(bytesToSend == toSend.length) 
                    fc= new FileChunk(toSend);
                else {
                    byte[] realArray = new byte[bytesToSend];
                    for(int j = 0; j<bytesToSend;j++) realArray[j] = toSend[j];
                    fc = new FileChunk(realArray);
                }
                oos.writeObject(fc);
            }
            
        } catch(IOException io) {
            io.printStackTrace();
        }
    }

    private void receiveFile() {
        try {
            String fileNameWithoutDirectory = fileName.substring(fileName.lastIndexOf('/')+1);
            String filePath = Paths.get(dirPath, fileNameWithoutDirectory).toString();
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

                FileChunk chunk = (FileChunk) ois.readObject(); // it is tcp so it is always in order
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