package com.DataTransfer;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.File;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import com.Client.Client;
import com.Client.conn.ClientConnection;
import com.enums.ResponseStatus;

public class ClientUpload extends Thread{
    private String fileName;
    private String replyWithPort;
    private InetAddress address;
    public static final int BLOCK_BYTE_SIZE = 8192;
    
    public ClientUpload(String fileName, String replyWithPort, InetAddress address) {
        this.fileName = fileName;
        this.replyWithPort = replyWithPort;
        this.address= address;
        this.start();
    }

    
    public void run() {
        try {
            sendFile();
        } catch(Exception e) {
            e.printStackTrace();
        }
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


    private void sendFile() throws SocketTimeoutException, SocketException {
        try {
            
            String[] portInfoSp = replyWithPort.split(" ");
            if (!portInfoSp[0].equals("PORT")) {
                System.out.println("Problems uploading file: " + replyWithPort);
                return;
            }
            int portNo = Integer.parseInt(portInfoSp[1]);
            Socket uploadSocket = new Socket(address, portNo);
            ObjectOutputStream oos = new ObjectOutputStream(uploadSocket.getOutputStream());
            oos.flush();
            ObjectInputStream ois = new ObjectInputStream(uploadSocket.getInputStream());
            Path filePath = Paths.get(Client.getClientDir(), fileName);
            long bytes = Files.size(filePath);
            long noBlocks = bytes / (FileTransfer.BLOCK_BYTE_SIZE)
                    + (bytes % (FileTransfer.BLOCK_BYTE_SIZE) == 0 ? 0 : 1);
            System.out.println();


            Reply rep = new Reply("FILE\n" + fileName + "\nSIZE\n" + bytes + "\nBLOCKS\n" + noBlocks,
                    ResponseStatus.OK.getStatus());
            System.out.println("Sending reply with file metadata: "+rep);
            oos.writeObject(rep);
            oos.flush();

            do {

                MessageDigest md = MessageDigest.getInstance("MD5");
                FileInputStream fis = new FileInputStream(new File(filePath.toString()));
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
                System.out.println("pos readobj: " + rep.getMessage());

            } while (!rep.getStatusCode().equals(ResponseStatus.OK.getStatus()));
            System.out.println("MD5 match");
        } catch(SocketTimeoutException | SocketException e) {
            throw e;
        } catch(IOException e){
            e.printStackTrace();
        }
        catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    
}
