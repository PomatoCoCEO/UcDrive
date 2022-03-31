package com.Server;

import com.DataTransfer.FileDownloadTask;
import com.DataTransfer.FileTransferDownloadCreator;
import com.DataTransfer.FileTransferUdpCreator;
import com.DataTransfer.FileTransferUploadCreator;
import com.DataTransfer.FileDownloadTask;
import com.DataTransfer.FileUploadTask;
import com.DataTransfer.UDPFileTransferTask;
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
import java.util.Scanner;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class Server {
    protected ServerSocket serverSocket;
    protected String absolutePath;
    protected final static int SOCKET_TIMEOUT_MILLISECONDS = 1000;
    protected final static int THREADS_PER_POOL = 10;
    protected final static int BLOCKING_QUEUE_SIZE = 100;
    protected Auth authenticationInfo;
    protected ConfigServer ownConfig;
    protected ConfigServer otherConfig;
    protected BlockingQueue<FileUploadTask> queueFileRcv;
    protected BlockingQueue<FileDownloadTask> queueFileSend;
    protected BlockingQueue<UDPFileTransferTask> queueUdp;
    protected ExecutorService threadPoolFileTasks;
    protected ExecutorService threadPoolTcpAccept;
    protected ExecutorService threadPoolUDPSend;
    protected ExecutorService threadPoolUDPReceive;

    public Server() {
    }

    public Server(String ownConfigFile, String otherConfigFile, String absPath) {
        this.absolutePath = absPath;
        try {
            System.out.println("Current directory: " + System.getProperty("user.dir"));

            authenticationInfo = new Auth("com/Server/runfiles/users", this);
            ownConfig = new ConfigServer("com/Server/runfiles/" + ownConfigFile);
            otherConfig = new ConfigServer("com/Server/runfiles/" + otherConfigFile);
            System.out.println("Server's own config: " + ownConfig);
            System.out.println("Server's mate config: " + otherConfig);
            serverSocket = new ServerSocket(ownConfig.getTcpSocketPort());
            System.out.println("LISTEN SOCKET=" + serverSocket);
            queueFileRcv = new LinkedBlockingQueue<>(BLOCKING_QUEUE_SIZE); // for file transfers with clients
            queueFileSend = new LinkedBlockingQueue<>(BLOCKING_QUEUE_SIZE); // for file transfers with downloads
            queueUdp = new LinkedBlockingQueue<>(BLOCKING_QUEUE_SIZE); // for file transfers with the secondary server
        } catch (IOException io) {
            io.printStackTrace();
            return;
        }
    }

    public void bePrimary() {
        // create threadPools
        threadPoolFileTasks = Executors.newFixedThreadPool(THREADS_PER_POOL);
        threadPoolUDPSend = Executors.newFixedThreadPool(THREADS_PER_POOL);
        threadPoolTcpAccept = Executors.newFixedThreadPool(THREADS_PER_POOL);
        TCPAccept tcpa = new TCPAccept(this);
        FileTransferUploadCreator fttc = new FileTransferUploadCreator(this);
        FileTransferDownloadCreator ftdc = new FileTransferDownloadCreator(this);
        FileTransferUdpCreator ftuc = new FileTransferUdpCreator(this);

        System.out.println("Hello there!");
        Scanner sc = new Scanner(System.in);
        while (true) {
            String s = sc.nextLine();
            if (s.equalsIgnoreCase("EXIT")) {
                // ? we know that this should be given more care...
                System.exit(1);
            } else {
                System.out.println("You typed: " + s);
            }
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
        return otherConfig;
    }

    public ExecutorService getThreadPoolFiles() {
        return threadPoolFileTasks;
    }

    public ExecutorService getThreadPoolUDPSend() {
        return threadPoolUDPSend;
    }

    public ExecutorService getThreadPoolUDPReceive() {
        return threadPoolUDPReceive;
    }

    public ExecutorService getThreadPoolTcpAccept() {
        return threadPoolTcpAccept;
    }

    public BlockingQueue<UDPFileTransferTask> getQueueUdp() {
        return queueUdp;
    }

    public BlockingQueue<FileUploadTask> getQueueFileRcv() {
        return queueFileRcv;
    }

    public BlockingQueue<FileDownloadTask> getQueueFileSend() {
        return queueFileSend;
    }

}
