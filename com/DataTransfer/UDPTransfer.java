package com.DataTransfer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import com.Client.Client;
import com.enums.ResponseStatus;

public class UDPTransfer extends Thread {
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private long byteSize, noBlocks;
    private String dirPath, fileName;
    public static final int BLOCK_BYTE_SIZE = 8192;
    private boolean send;

    public UDPTransfer(ObjectInputStream ois, ObjectOutputStream oos, long byteSize, long noBlocks, String dirPath,
            String fileName, boolean send) {
        this.ois = ois;
        this.oos = oos;
        this.byteSize = byteSize;
        this.noBlocks = noBlocks;
        this.dirPath = dirPath;
        this.fileName = fileName;
        this.send = send;
        this.start();
    }

    public void run() {
        if (send)
            sendFile();
        else
            receiveFile();
    }

    private void sendFile() {
        try {
            // String fileName = absolutePath.substring(absolutePath.lastIndexOf("/")+1); //
            // for linux and mac
            // if(fileName.indexOf("\\")!=-1) fileName =
            // absolutePath.substring(absolutePath.lastIndexOf("\\")+1); // for windows
            Reply rep = new Reply("FILE\n" + fileName + "\nSIZE\n" + byteSize + "\nBLOCKS\n" + noBlocks,
                    ResponseStatus.OK.getStatus());
            oos.writeObject(rep);
            oos.flush();

            do {

                MessageDigest md = MessageDigest.getInstance("MD5");
                FileInputStream fis = new FileInputStream(new File(Paths.get(dirPath, fileName).toString()));
                DigestInputStream dis = new DigestInputStream(fis, md);

                byte[] toSend = new byte[BLOCK_BYTE_SIZE];
                for (int i = 0; i < noBlocks; i++) {
                    int bytesToSend = dis.read(toSend);

                    FileChunk fc = new FileChunk(Arrays.copyOf(toSend, bytesToSend));
                    oos.writeObject(fc);
                    oos.flush();
                }
                byte[] digest = md.digest();

                String result = "";

                for (int i = 0; i < digest.length; i++) {
                    result += Integer.toString((digest[i] & 0xff) + 0x100, 16).substring(1);
                }
                System.out.println("MD5 result: " + result);

                rep = new Reply(result, ResponseStatus.OK.getStatus());
                oos.writeObject(rep);
                oos.flush();

                rep = (Reply) ois.readObject();

            } while (!rep.getStatusCode().equals(ResponseStatus.OK.getStatus()));
            System.out.println("MD5 match");
        } catch (IOException io) {
            io.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void receiveFile() {
        try {
            Reply rep;
            do {

                String fileNameWithoutDirectory = fileName.substring(fileName.lastIndexOf('/') + 1);
                String filePath = Paths.get(dirPath, fileNameWithoutDirectory).toString();
                File myObj = new File(filePath);
                if (myObj.createNewFile()) {
                    System.out.println("File created: " + myObj.getName());
                } else {

                    // ! give the file another name maybe

                    System.out.println("File already exists.");
                }
                FileOutputStream fos = new FileOutputStream(myObj);
                MessageDigest md = MessageDigest.getInstance("MD5");
                for (int i = 0; i < noBlocks; i++) {

                    // ! check last block size ??

                    FileChunk chunk = (FileChunk) ois.readObject(); // it is tcp so it is always in order
                    // ! we might need to cache this
                    fos.write(chunk.getBytes());
                    fos.flush();

                    md.update(chunk.getBytes(), 0, chunk.getNumBytes());

                }
                fos.close();

                byte[] digest = md.digest();

                String result = "";

                for (int i = 0; i < digest.length; i++) {
                    result += Integer.toString((digest[i] & 0xff) + 0x100, 16).substring(1);
                }
                System.out.println("MD5 result: " + result);

                rep = (Reply) ois.readObject();
                if (rep.getMessage().equals(result)) {

                    rep = new Reply(result, ResponseStatus.OK.getStatus());
                } else {
                    rep = new Reply("MD5 results do not match, trying again.", "Bad request");
                    System.out.println(rep.getMessage());
                    myObj.delete();
                }
                oos.writeObject(rep);
                oos.flush();
            } while (!rep.getStatusCode().equals(ResponseStatus.OK.getStatus()));

            System.out.println(fileName + " : transfer complete");
            ois.close();
            oos.close();
        } catch (IOException e) {
            System.out.println("An error occurred:");
            e.printStackTrace();
        } catch (ClassNotFoundException cnf) {
            System.out.println("An error occurred:");
            cnf.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}