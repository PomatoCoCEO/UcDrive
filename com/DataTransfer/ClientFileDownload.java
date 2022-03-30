package com.DataTransfer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import com.Client.Client;
import com.Server.Server;
import com.enums.ResponseStatus;

public class ClientFileDownload extends Thread {

    private String dirPath;
    public static final int BLOCK_BYTE_SIZE = 8192;
    private ServerSocket serverSocket;

    public ClientFileDownload(ServerSocket serverSocket, String clientDir) {
        this.dirPath= clientDir; // ! check this
        this.serverSocket = serverSocket;
        this.start();
	}
    // public ClientFileDownload(FileTransferDownloadTask ftdt, Server server){
    //     try {
    //         this.oos = new ObjectOutputStream(ftdt.getSocket().getOutputStream());
    //         oos.flush();
    //         this.ois = new ObjectInputStream(ftdt.getSocket().getInputStream());
    //         this.byteSize = ftdt.getByteSize();
    //         this.noBlocks = ftdt.getNumberOfBlocks();
    //         this.dirPath = ftdt.getDirPath();
    //         this.fileName = ftdt.getFileName();
    //         this.send = true; // only called by the server
    //         // this.server= server;
    //         this.start();
    //     } catch(IOException io) {
    //         io.printStackTrace();
    //     }
    // }

    

    // public ClientFileDownload(ObjectOutputStream oos, ObjectInputStream ois, long byteSize, long noBlocks, String dirPath,
    //         String fileName, boolean send) {
        
    //     this.oos = oos;
    //     this.ois = ois;
    //     this.byteSize = byteSize;
    //     this.noBlocks = noBlocks;
    //     this.dirPath = dirPath;
    //     this.fileName = fileName;
    //     this.send = false; // only called by the client 
    //     this.start();
    // }




	public void run() {
        receiveFile();
    }
    
    /**
     * Calculate the MD5 hash of the input string and return the hash as a hex string
     * 
     * @param md The MessageDigest object that contains the digest algorithm.
     * @return The MD5 hash of the input string.
     */
    private String calculateMD5(MessageDigest md) {
        StringBuilder sb = new StringBuilder();
        byte[] bytes = md.digest();

        for (int i = 0; i < bytes.length; i++) {
            sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }

    private void receiveFile() {
        try {

            Reply rep;
            Socket receiver = serverSocket.accept();
            System.out.println("Receiver information: "+receiver.getLocalAddress()+":"+receiver.getLocalPort());
            ObjectOutputStream oos = new ObjectOutputStream(receiver.getOutputStream());
            oos.flush();
            ObjectInputStream ois = new ObjectInputStream(receiver.getInputStream());

            //! this yields something bad...
            rep = (Reply) ois.readObject();
            String fileMetaData = rep.getMessage();
            String[] fileDataSplit = fileMetaData.split("\n");
            if (fileDataSplit.length < 6 ||
                    !fileDataSplit[0].equals("FILE") ||
                    !fileDataSplit[2].equals("SIZE") ||
                    !fileDataSplit[4].equals("BLOCKS")) {
                System.err.println("Errors communicating with the server");
                return;
            }
            String fileName = fileDataSplit[1];
            long byteSize = Integer.parseInt(fileDataSplit[3]);
            long noBlocks = Integer.parseInt(fileDataSplit[5]);
            System.out.printf("Name: %s, byteSize: %d, BlockNumber: %s\n", fileName, byteSize, noBlocks);

            String filePath;
            do {

                String fileNameWithoutDirectory = fileName.substring(fileName.lastIndexOf('/') + 1);
                filePath = Paths.get(dirPath, fileNameWithoutDirectory).toString();
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

                    FileChunk chunk = (FileChunk) ois.readObject(); // it is tcp so it is always in order
                    // ! we might need to cache this
                    fos.write(chunk.getBytes());
                    fos.flush();

                    md.update(chunk.getBytes(), 0, chunk.getNumBytes());

                }
                fos.close();

                String result= calculateMD5(md);
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