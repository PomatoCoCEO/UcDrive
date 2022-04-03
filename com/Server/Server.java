package com.Server;

import com.DataTransfer.creators.FileTransferDownloadCreator;
import com.DataTransfer.creators.FileTransferUdpCreator;
import com.DataTransfer.creators.FileTransferUploadCreator;
import com.DataTransfer.tasks.FileDownloadTask;
import com.DataTransfer.tasks.FileUploadTask;
import com.DataTransfer.tasks.UDPFileTransferTask;
import com.DataTransfer.threads.UDPTransfer;
import com.Server.auth.*;
import com.Server.config.ConfigServer;
import com.Server.conn.ServerConnection;
import com.Server.tcp.TCPAccept;

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
    protected String absolutePath, configPath;
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
    protected ExecutorService threadPoolUDPCommandSend;

    public Server() {
    }

    public Server(String ownConfigFile, String otherConfigFile, String absPath, String configPath) {
        this.absolutePath = absPath;
        this.configPath = configPath;
        try {
            System.out.println("Current directory: " + System.getProperty("user.dir"));

            authenticationInfo = new Auth(this);
            ownConfig = new ConfigServer(Paths.get(configPath, ownConfigFile).toString());
            otherConfig = new ConfigServer(Paths.get(configPath, otherConfigFile).toString());
            System.out.println("Server's own config: " + ownConfig);
            System.out.println("Server's mate config: " + otherConfig);

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
        try {
            authenticationInfo = new Auth(this);
            serverSocket = new ServerSocket(ownConfig.getTcpSocketPort());
            System.out.println("LISTEN SOCKET=" + serverSocket);
        } catch (IOException io) {
            io.printStackTrace();
            return;
        }
        threadPoolFileTasks = Executors.newFixedThreadPool(THREADS_PER_POOL);
        threadPoolUDPSend = Executors.newFixedThreadPool(THREADS_PER_POOL);
        threadPoolTcpAccept = Executors.newFixedThreadPool(THREADS_PER_POOL);
        threadPoolUDPCommandSend = Executors.newFixedThreadPool(THREADS_PER_POOL);
        TCPAccept tcpa = new TCPAccept(this);
        FileTransferUploadCreator fttc = new FileTransferUploadCreator(this);
        FileTransferDownloadCreator ftdc = new FileTransferDownloadCreator(this);
        FileTransferUdpCreator ftuc = new FileTransferUdpCreator(this);

        Scanner sc = new Scanner(System.in);
        while (true) {
            String s = sc.nextLine();
            System.out.println("You typed: " + s);
        }
    }

    public String getAbsolutePath() {
        return absolutePath;
    }

    public String getConfigPath() {
        return configPath;
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

    public ExecutorService getThreadPoolUdpCommandSend() {
        return threadPoolUDPCommandSend;
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
