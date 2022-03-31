package com.DataTransfer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.Server.conn.ServerConnection;
import com.enums.ResponseStatus;

public class ServerUpload extends Thread {
    private ServerConnection serverConnection;
    private ServerSocket serverSocket;

    public ServerUpload(ServerConnection sc, ServerSocket serverSocket) {
        this.serverConnection = sc;
        this.serverSocket = serverSocket;
        this.start();
    }

    public void run() {
        ServerSocket listenUploadSocket = serverSocket;
        try {
            // try(ServerSocket listenUploadSocket = new ServerSocket(0)) {
            // old code
            // sends port so client sends metadata here

            // new socket to receive file
            Socket uploadSocket = listenUploadSocket.accept();
            ObjectOutputStream oos = new ObjectOutputStream(uploadSocket.getOutputStream());
            oos.flush();
            ObjectInputStream ois = new ObjectInputStream(uploadSocket.getInputStream());
            // receive metadata
            Reply rep = (Reply) ois.readObject();
            String fileMetaData = rep.getMessage();
            String[] fileDataSplit = fileMetaData.split("\n");
            if (fileDataSplit.length < 6 ||
                    !fileDataSplit[0].equals("FILE") ||
                    !fileDataSplit[2].equals("SIZE") ||
                    !fileDataSplit[4].equals("BLOCKS")) {
                System.err.println("Errors communicating with the server");
                return;
            }

            // file parameters
            String fileName = fileDataSplit[1];
            long byteSize = Integer.parseInt(fileDataSplit[3]);
            long blockNumber = Integer.parseInt(fileDataSplit[5]);
            System.out.printf("Name: %s, byteSize: %d, BlockNumber: %s\n", fileName, byteSize, blockNumber);
            String dirPath = Paths.get(serverConnection.getAbsolutePath(), serverConnection.getUser().getServerDir())
                    .toString();
            // ! use threadpool here
            String filePath;
            String fileNameWithoutDirectory;
            do {

                fileNameWithoutDirectory = fileName.substring(fileName.lastIndexOf('/') + 1);
                filePath = Paths.get(dirPath, fileNameWithoutDirectory).toString();
                File myObj = new File(filePath);
                if (myObj.createNewFile()) {
                    System.out.println("File created: " + myObj.getName());
                } else {

                    // ! give the file another name maybe

                    System.out.println("File already exists. Removing existing one");
                }
                FileOutputStream fos = new FileOutputStream(myObj);
                MessageDigest md = MessageDigest.getInstance("MD5");
                for (int i = 0; i < blockNumber; i++) {

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
            uploadSocket.close();
            System.out.println(fileName + " : transfer complete. Now handling udp");

            filePath = Paths.get(serverConnection.getUser().getServerDir(), fileNameWithoutDirectory).toString();
            UDPFileTransferTask uftt = new UDPFileTransferTask(
                    byteSize, blockNumber, serverConnection.getAbsolutePath(), filePath, true,
                    serverConnection.getServer().getDestinationConfig().getServerAddress(),
                    serverConnection.getServer().getDestinationConfig().getUdpFileTransferPort());
            System.out.println("adding uftt to queue ONCE");
            serverConnection.getServer().getQueueUdp().add(uftt);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
}
