package com.DataTransfer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import com.Client.Client;
import com.DataTransfer.chunks.FileChunk;
import com.DataTransfer.props.Properties;
import com.Server.Server;
import com.enums.ResponseStatus;

public class FileTransfer extends Thread {
    protected ObjectInputStream ois;
    protected ObjectOutputStream oos;
    protected long byteSize, noBlocks;
    protected String dirPath, fileName;
    public static final int BLOCK_BYTE_SIZE = Properties.BLOCK_BYTE_SIZE;
    protected boolean send;

    public FileTransfer(){
    
    }

    public FileTransfer(InetAddress ipAddress, int port, long byteSize, long noBlocks, String dirPath,
            String fileName, boolean send) {
        try {
            Socket s = new Socket(ipAddress, port);
            this.oos = new ObjectOutputStream(s.getOutputStream());
            oos.flush();
            this.ois = new ObjectInputStream(s.getInputStream());
        } catch(IOException io) {
            io.printStackTrace();
        }
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

            Reply rep = new Reply("FILE\n" + fileName + "\nSIZE\n" + byteSize + "\nBLOCKS\n" + noBlocks,
                    ResponseStatus.OK.getStatus());
            System.out.println("Sending reply with file metadata: "+rep);
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

    protected void receiveFile() {
        try {
            Reply rep;
            String filePath;
            do {

                String fileNameWithoutDirectory = fileName.substring(fileName.lastIndexOf('/') + 1);
                filePath = Paths.get(dirPath, fileNameWithoutDirectory).toString();
                File myObj = new File(filePath);
                /*if (myObj.createNewFile()) {
                    System.out.println("File created: " + myObj.getName());
                } else {

                    // ! give the file another name maybe

                    System.out.println("File already exists.");
                }*/
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
                // System.out.println("MD5 result: " + result);

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
            // ! add to blocking queue
            
            ois.close();
            oos.close();
        } catch (IOException e) {
            System.out.println("An error occurred: "+e.getMessage());
            e.printStackTrace();
        } catch (ClassNotFoundException cnf) {
            System.out.println("An error occurred: "+cnf.getMessage());
            cnf.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}