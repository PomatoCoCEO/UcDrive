package com.Server;

import com.Server.auth.*;
import com.Server.config.ConfigServer;
import com.Server.conn.ServerConnection;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private static ServerSocket serverSocket;

    public static void main(String[] args) {
        try {
            Auth authenticationInfo = new Auth("com/Server/runfiles/users");
            ConfigServer config = new ConfigServer("com/Server/runfiles/config");
            ServerSocket listenSocket = new ServerSocket(config.getSocketPort());
            System.out.println("LISTEN SOCKET=" + listenSocket);
            while (true) {
                Socket clientSocket = listenSocket.accept(); // BLOQUEANTE
                System.out.println("CLIENT_SOCKET (created at accept())=" + clientSocket);
                new ServerConnection(clientSocket, authenticationInfo);
            }

        } catch (IOException io) {
            io.printStackTrace();
            return;
        }
    }
}
