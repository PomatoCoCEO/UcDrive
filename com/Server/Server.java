package com.Server;

import com.DataTransfer.UDPTransfer;
import com.Server.auth.*;
import com.Server.config.ConfigServer;
import com.Server.conn.ServerConnection;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.ServerError;

public class Server {
    protected ServerSocket serverSocket;
    protected String absolutePath;
    protected final static int SOCKET_TIMEOUT_MILLISECONDS = 5000;
    protected Auth authenticationInfo;
    protected ConfigServer ownConfig;
    protected ConfigServer redundantConfig;
    

    public Server(String ownConfigFile, String otherConfigFile) {
        try {
            System.out.println("Current directory: " + System.getProperty("user.dir"));

            authenticationInfo = new Auth("com/Server/runfiles/users");
            ownConfig = new ConfigServer("com/Server/runfiles/" + ownConfigFile);
            redundantConfig = new ConfigServer("com/Server/runfiles/" + otherConfigFile);
            System.out.println("Server's own config: "+ownConfig);
            System.out.println("Server's mate config: "+redundantConfig);
            serverSocket = new ServerSocket(ownConfig.getTcpSocketPort());
            System.out.println("LISTEN SOCKET=" + serverSocket);
        } catch (IOException io) {
            io.printStackTrace();
            return;
        }
    }

    protected void acceptTcp() {
        try {
            while (true) {
                Socket clientSocket = serverSocket.accept(); // BLOQUEANTE
                System.out.println("CLIENT_SOCKET (created at accept())=" + clientSocket);
                new ServerConnection(clientSocket, this);
            }
        } catch (IOException io) {
            io.printStackTrace();
            return;
        }
    }

    protected void acceptUdp() {
        // TODO: accept udp connections for file updating
        try {
            DatagramSocket ds = new DatagramSocket(ownConfig.getUdpFileTransferPort());
            while (true) {
                try {
                    byte[] buffer = new byte[1000];
                    DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
                    ds.receive(reply); // no timeout here, this socket is waiting for connections
                    String message = new String(reply.getData()); // FILE <FILE_PATH>\nSIZE <BYTE_SIZE>\nBLOCKS
                                                                  // <BLOCKS>\nPORT <PORT_NUMBER>
                    String[] elements = message.split("\n");
                    String filePath = elements[0].split(" ", 2)[1];
                    long size = Long.parseLong(elements[1].split(" ", 2)[1]);
                    int noBlocks = Integer.parseInt(elements[2].split(" ", 2)[1]);
                    int portNum = Integer.parseInt(elements[3].split(" ", 2)[1]);
                    // send OK and start to download the file
                    // ! this might not be the correct port
                    new UDPTransfer(size, noBlocks, absolutePath, filePath,
                            false, reply.getAddress(), reply.getPort());

                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public String getAbsolutePath() {
        return absolutePath;
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }
    
    public Auth getAuthInfo() {
        return authenticationInfo;
    }

    public ConfigServer getConfig() {
        return ownConfig;
    }

    public ConfigServer getDestinationConfig() {
        return redundantConfig;
    }
    

}
