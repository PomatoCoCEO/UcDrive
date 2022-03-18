package com.Server;

import com.Server.auth.*;
import com.Server.config.ConfigServer;
import com.Server.conn.ServerConnection;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Server {
    private static ServerSocket serverSocket;
    private static String absolutePath;

    public static void main(String[] args) {
        try {
            System.out.println("Current directory: " + System.getProperty("user.dir"));
            absolutePath = System.getProperty("user.dir") + "/com/Server/data";
            Auth authenticationInfo = new Auth("com/Server/runfiles/users");
            ConfigServer config = new ConfigServer("com/Server/runfiles/config");
            ServerSocket listenSocket = new ServerSocket(config.getSocketPort());
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

    public static String getAbsolutePath() {
        return absolutePath;
    }
}
