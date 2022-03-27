package com.Server.file_transfer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.DataTransfer.FileChunk;
import com.DataTransfer.Reply;

public class FileTransfer extends Thread {
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private int byteSize, noBlocks;
    private String fileName;
    private final int BLOCK_BYTE_SIZE = 8192;
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
            Reply rep = new Reply("FILE\n" + fileName +"\nSIZE\n"+byteSize+"\nBLOCKS\n"+noBlocks, "OK");
            oos.writeObject(rep);
            FileInputStream fis = new FileInputStream(new File(fileName));
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

}
