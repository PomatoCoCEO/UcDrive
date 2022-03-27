package com.Server;

import com.Server.auth.*;
import com.Server.config.ConfigServer;
import com.Server.conn.ServerConnection;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SecondaryServer extends Server {

    public SecondaryServer(String configFile) {
        super(configFile);
    }

    public void work() {

        absolutePath = System.getProperty("user.dir") + "/com/Server/secondary/data";

        try {
            ConfigServer primaryServerConfig = new ConfigServer("com/Server/runfiles/Pconfig");
            DatagramSocket ds = new DatagramSocket();
            ds.setSoTimeout(SOCKET_TIMEOUT_MILLISECONDS);

        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // send I AM SECONDARY
        // create heartbeat thread
        // create receive files thread that shuts down if server becomes primary
        // if PRIMARY does not responde 5 times to heartbeat, become primary and start
        // sending I AM PRIMARY
        // and start listening to TCP connections

        try {
            System.out.println("Current directory: " + System.getProperty("user.dir"));
            absolutePath = System.getProperty("user.dir") + "/com/Server/data";
            Auth authenticationInfo = new Auth("com/Server/runfiles/users");
            ConfigServer config = new ConfigServer("com/Server/runfiles/Sconfig");
            ServerSocket listenSocket = new ServerSocket(config.getTcpSocketPort());
            System.out.println("LISTEN SOCKET=" + listenSocket);
            while (true) {
                Socket clientSocket = listenSocket.accept(); // BLOQUEANTE
                System.out.println("CLIENT_SOCKET (created at accept())=" + clientSocket);
                new ServerConnection(clientSocket, authenticationInfo, absolutePath);
            }

        } catch (IOException io) {
            io.printStackTrace();
            return;
        }

    }

    public static void main(String[] args) {
        SecondaryServer ss = new SecondaryServer("Sconfig");
        ss.work();

    }

}
