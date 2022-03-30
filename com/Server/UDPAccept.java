package com.Server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;

import com.DataTransfer.UDPTransfer;

public class UDPAccept extends Thread {
    private Server server;
    public UDPAccept(Server server) {
        this.server = server;
        this.start();
    }


    private void acceptUdp() {
        // TODO: accept udp connections for file updating
        try {
            DatagramSocket ds = new DatagramSocket(server.getConfig().getUdpFileTransferPort());
            while (true) {
                try {
                    byte[] buffer = new byte[1000];
                    DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
                    ds.receive(reply); // no timeout here, this socket is waiting for connections
                    String message = new String(reply.getData()).trim(); // FILE <FILE_PATH>\nSIZE <BYTE_SIZE>\nBLOCKS
                                                                  // <BLOCKS>\nPORT <PORT_NUMBER>
                    System.out.println("Message: "+message);
                    String[] elements = message.split("\n");
                    String filePath = elements[0].split(" ", 2)[1];
                    long size = Long.parseLong(elements[1].split(" ", 2)[1]);
                    int noBlocks = Integer.parseInt(elements[2].split(" ", 2)[1]);
                    int portNum = Integer.parseInt(elements[3].split(" ", 2)[1]);
                    // ! this might not be the correct port
                    new UDPTransfer(size, noBlocks, server.getAbsolutePath(), filePath,
                            false, reply.getAddress(), reply.getPort());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        acceptUdp();
    }
}
