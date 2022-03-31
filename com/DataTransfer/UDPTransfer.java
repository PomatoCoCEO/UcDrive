package com.DataTransfer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

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

    public UDPTransfer(UDPFileTransferTask uftt) {
        this.byteSize = uftt.getByteSize();
        this.noBlocks = uftt.getNoBlocks();
        this.filePath = uftt.getFilePath();
        this.absolutePath = uftt.getAbsolutePath();
        this.send = uftt.isSend();
        this.destinationAddress = uftt.getDestinationAddress();
        this.destinationPort = uftt.getDestinationPort();
        // ! this.start(); was taken because the executor handles it
    }

    public void run() {
        System.out.println("EXECUTING UDP TRANSFER");
        if (send)
            sendFile();
        else
            receiveFile();
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

    /**
     * Send a string to the destination address and port
     * 
     * @param ds The DatagramSocket object that will be used to send the message.
     * @param s  The string to send.
     */
    private void sendString(DatagramSocket ds, String s) {
        DatagramPacket dp = new DatagramPacket(s.getBytes(), s.length(), destinationAddress, destinationPort);
        try {
            ds.send(dp);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Receive a packet from a socket and return the bytes of the packet
     * 
     * @param ds     The DatagramSocket object that we created earlier.
     * @param length The number of bytes to receive.
     * @return The byte array of the received message.
     */
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

    /**
     * Send a file to the destination
     */
    private void sendFile() {
        try {
            // String fileName = absolutePath.substring(absolutePath.lastIndexOf("/")+1); //
            // for linux and mac
            // if(fileName.indexOf("\\")!=-1) fileName =
            // absolutePath.substring(absolutePath.lastIndexOf("\\")+1); // for windows
            DatagramSocket ds = new DatagramSocket();
            String fileInfo = "FILE " + filePath + "\nSIZE " + byteSize + "\nBLOCKS " + noBlocks + "\nPORT "
                    + destinationPort;
            sendString(ds, fileInfo);
            System.out.println("File metadata to send: " + fileInfo);
            // read ok
            // change destination port
            byte[] reply = new byte[BLOCK_BYTE_SIZE];
            DatagramPacket dp = new DatagramPacket(reply, reply.length);
            ds.receive(dp);
            destinationPort = dp.getPort();

            MessageDigest md = MessageDigest.getInstance("MD5");
            String fileCompletePath = Paths.get(absolutePath, filePath).toString();
            FileInputStream fis = new FileInputStream(new File(fileCompletePath));
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
                System.out.println("Sent block tranch");
                String md5Secondary = new String(receiveBytes(ds, (int) BLOCK_BYTE_SIZE)).trim();
                String md5Result = calculateMD5(md);
                System.out.println("MD5s : " + md5Secondary + " and " + md5Result);
                if (!md5Result.equals(md5Secondary)) {
                    newInfo = false;
                    sendString(ds, "ERROR MD5");
                    System.out.println("DIFFERENT MD5 " + md5Secondary + " " + md5Result);
                } else {
                    System.out.println("Sending ok...");
                    newInfo = true;
                    sendString(ds, "OK");
                    blocksSent += NO_BLOCKS_TRANSFER;
                }
            }

            System.out.println("Udp transfer complete");
            dis.close();
        } catch (IOException io) {
            io.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Receive a file from a client
     */
    private void receiveFile() {
        try {
            int blocksRead = 0;

            DatagramSocket ds = new DatagramSocket();
            sendString(ds, "OK");
            ds.setSoTimeout(UDP_SOCKET_TIMEOUT);
            System.out.println("Sent ok");
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
            int[] lengths = new int[NO_BLOCKS_TRANSFER];
            System.out.println("Blocks read: " + blocksRead);
            System.out.println("noBlocks: " + noBlocks);
            while (blocksRead < noBlocks) {
                clone = (MessageDigest) md.clone();
                for (int i = 0; i < Math.min(noBlocks - blocksRead, NO_BLOCKS_TRANSFER); i++) {
                    // int aid =
                    DatagramPacket reply = new DatagramPacket(cache[i], cache[i].length);
                    ds.receive(reply); // ! use timeout here
                    lengths[i] = reply.getLength();
                    // System.out.println("Received block: " + new String(reply.getData()));
                    md.update(cache[i], 0, reply.getLength());
                }
                String md5 = calculateMD5(md);
                sendString(ds, md5);
                System.out.println("Waiting for ok...");
                String ans = new String(receiveBytes(ds, BLOCK_BYTE_SIZE)).trim();
                System.out.println("ans = " + ans);
                if (ans.equals("OK")) {
                    for (int i = 0; i < Math.min(noBlocks - blocksRead, NO_BLOCKS_TRANSFER); i++) {
                        if (lengths[i] < BLOCK_BYTE_SIZE) {
                            byte[] toCopy = Arrays.copyOf(cache[i], lengths[i]);
                            fos.write(toCopy);
                        } else
                            fos.write(cache[i]);
                        fos.flush();
                    }
                    blocksRead += Math.min(noBlocks - blocksRead, NO_BLOCKS_TRANSFER);
                } else { // SENDS "ERROR MD5"
                    md = clone;
                    System.out.println("Message: " + ans);
                    System.out.println("Error in udp transfer, trying again");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
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