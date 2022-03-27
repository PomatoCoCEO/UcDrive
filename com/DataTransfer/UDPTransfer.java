package com.DataTransfer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import com.Client.Client;
import com.enums.ResponseStatus;

public class UDPTransfer extends Thread {

    private long byteSize, noBlocks;
    private String absolutePath;
    private String filePath;
    private boolean send;
    private InetAddress destinationAddress;
    private int destinationPort;
    public static final int BLOCK_BYTE_SIZE = 8192;
    public static final int NO_BLOCKS_TRANSFER = 16;
    public static final int UDP_SOCKET_TIMEOUT = 5000;

    public UDPTransfer(long byteSize, long noBlocks, String absolutePath, String filePath, boolean send,
            InetAddress destinationAddress, int destinationPort) {
        this.byteSize = byteSize;
        this.noBlocks = noBlocks;
        this.filePath = filePath;
        this.absolutePath = absolutePath;
        this.send = send;
        this.destinationAddress = destinationAddress;
        this.destinationPort = destinationPort;
        this.start();
    }

    public void run() {
        if (send)
            sendFile();
        else
            receiveFile();
    }

    private String calculateMD5(MessageDigest md) {
        StringBuilder sb = new StringBuilder();
        byte[] bytes = md.digest();

        for (int i = 0; i < bytes.length; i++) {
            sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }

    private void sendString(DatagramSocket ds, String s) {
        DatagramPacket dp = new DatagramPacket(s.getBytes(), s.length(), destinationAddress, destinationPort);
        try {
            ds.send(dp);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private byte[] receiveBytes(DatagramSocket ds, int length) {
        byte[] buffer = new byte[length];
        DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
        try {
            ds.receive(reply);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return buffer;
    }

    private void sendFile() {
        try {
            // String fileName = absolutePath.substring(absolutePath.lastIndexOf("/")+1); //
            // for linux and mac
            // if(fileName.indexOf("\\")!=-1) fileName =
            // absolutePath.substring(absolutePath.lastIndexOf("\\")+1); // for windows
            DatagramSocket ds = new DatagramSocket();
            String fileInfo = "FILE " + filePath + "\nSIZE " + byteSize + "\nBLOCKS " + noBlocks + "\nPORT "
                    + ds.getPort();
            sendString(ds, fileInfo);

            // read ok
            // change destination port
            byte[] reply = new byte[BLOCK_BYTE_SIZE];
            DatagramPacket dp = new DatagramPacket(reply, reply.length);
            ds.receive(dp);
            destinationPort = dp.getPort();

            MessageDigest md = MessageDigest.getInstance("MD5");
            FileInputStream fis = new FileInputStream(new File(filePath));
            DigestInputStream dis = new DigestInputStream(fis, md);

            byte[][] cache = new byte[NO_BLOCKS_TRANSFER][BLOCK_BYTE_SIZE];
            int[] lengths = new int[NO_BLOCKS_TRANSFER];
            int blocksSent = 0;
            boolean newInfo = true;

            while (blocksSent < noBlocks) {

                for (int i = 0; i < Math.min(noBlocks - blocksSent, NO_BLOCKS_TRANSFER); i++) {
                    if (newInfo) {
                        lengths[i] = dis.read(cache[i]);
                    }
                    DatagramPacket dpBlock = new DatagramPacket(cache[i], lengths[i], destinationAddress,
                            destinationPort);
                    ds.send(dpBlock);
                }

                String md5Secondary = new String(receiveBytes(ds, (int) BLOCK_BYTE_SIZE));
                String md5Result = calculateMD5(md);
                if (!md5Result.equals(md5Secondary)) {
                    newInfo = false;
                    sendString(ds, "ERROR MD5");
                } else {
                    newInfo = true;
                    sendString(ds, "OK");
                }
            }

            System.out.println("Udp transfer complete");
        } catch (IOException io) {
            io.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void receiveFile() {
        try {
            int blocksRead = 0;

            DatagramSocket ds = new DatagramSocket();
            sendString(ds, "OK");
            ds.setSoTimeout(UDP_SOCKET_TIMEOUT);
            String path = Paths.get(absolutePath, filePath).toString();
            File myObj = new File(path);
            if (myObj.createNewFile()) {
                System.out.println("File created: " + myObj.getName());
            } else {

                // ! give the file another name maybe

                System.out.println("File already exists.");
            }
            FileOutputStream fos = new FileOutputStream(myObj);
            MessageDigest md = MessageDigest.getInstance("MD5"), clone;
            byte[][] cache = new byte[NO_BLOCKS_TRANSFER][BLOCK_BYTE_SIZE];
            while (blocksRead < noBlocks) {
                clone = (MessageDigest) md.clone();
                for (int i = 0; i < Math.min(noBlocks - blocksRead, NO_BLOCKS_TRANSFER); i++) {

                    DatagramPacket reply = new DatagramPacket(cache[i], cache[i].length);
                    ds.receive(reply); // no timeout here, this socket is waiting for connections

                    md.update(cache[i], 0, cache[i].length);

                }
                String md5 = calculateMD5(md);
                sendString(ds, md5);
                String ans = new String(receiveBytes(ds, BLOCK_BYTE_SIZE));
                if (ans.equals("OK")) {
                    for (int i = 0; i < Math.min(noBlocks - blocksRead, NO_BLOCKS_TRANSFER); i++) {
                        fos.write(cache[i]);
                        fos.flush();
                    }
                    blocksRead += Math.min(noBlocks - blocksRead, NO_BLOCKS_TRANSFER);
                } else { // SENDS "ERROR MD5"
                    md = clone;
                    System.out.println("Error in udp transfer, trying again");

                }

            }

            // check md5 and if its correct write to file

            fos.close();

            System.out.println(filePath + " : transfer complete");

        } catch (IOException e) {
            System.out.println("An error occurred:");
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (CloneNotSupportedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}