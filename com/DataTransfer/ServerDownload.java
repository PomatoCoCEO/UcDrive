package com.DataTransfer;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import com.Client.Client;
import com.Server.Server;
import com.Server.conn.ServerConnection;
import com.enums.ResponseStatus;

public class ServerDownload extends Thread {

    private String filePath;
    public static final int BLOCK_BYTE_SIZE = 8192;
    private InetAddress address;
    private int portNo;
    private ServerConnection serverConnection;

    public ServerDownload(InetAddress inetAddress, int portNo, String filePath, ServerConnection serverConnection) {
        this.address = inetAddress;
        this.portNo = portNo;

        this.filePath = filePath;
        this.serverConnection = serverConnection;
        this.start();
    }

    public ServerDownload(FileDownloadTask fdt) {
        this.address = fdt.getAddress();
        this.portNo = fdt.getPortNo();

        this.filePath = fdt.getFilePath();
        this.serverConnection = fdt.getServerConnection();
        this.start();
    }
    // public ServerDownload(ServerConnection serverConnection, String filePath) {
    // this.serverConnection = serverConnection;
    // this.filePath=filePath;
    // }

    public void run() {
        sendFile();
    }

    /**
     * Calculate the MD5 hash of the input string and return the hash as a hex
     * string
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

    private void sendFile() {
        try {

            String absoluteDirPath = serverConnection.getServer().getAbsolutePath();

            Socket sendSocket = new Socket(address, portNo);
            ObjectOutputStream oos = new ObjectOutputStream(sendSocket.getOutputStream());
            oos.flush();
            ObjectInputStream ois = new ObjectInputStream(sendSocket.getInputStream());
            Path absolutePath = Paths.get(absoluteDirPath, filePath);
            long bytes = Files.size(absolutePath);
            long noBlocks = bytes / (FileTransfer.BLOCK_BYTE_SIZE)
                    + (bytes % (FileTransfer.BLOCK_BYTE_SIZE) == 0 ? 0 : 1);
            System.out.println();
            String dirPath = Paths.get(serverConnection.getAbsolutePath(), serverConnection.getUser().getServerDir())
                    .toString();

            Reply rep = new Reply("FILE\n" + filePath + "\nSIZE\n" + bytes + "\nBLOCKS\n" + noBlocks,
                    ResponseStatus.OK.getStatus());
            System.out.println("Sending reply with file metadata: " + rep);
            oos.writeObject(rep);
            oos.flush();

            do {

                MessageDigest md = MessageDigest.getInstance("MD5");
                FileInputStream fis = new FileInputStream(new File(Paths.get(absoluteDirPath, filePath).toString()));
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

}