package com.Server;

import java.io.IOException;
import java.net.Socket;

import com.Server.conn.ServerConnection;

public class TCPAccept extends Thread {
    private Server server;

    public TCPAccept(Server server) {
        this.server = server;
        this.start();
    }

    public void run () {
        acceptTcp();
    }
    
    private void acceptTcp() {
        try {
            while (true) {
                Socket clientSocket = server.getServerSocket().accept(); // BLOQUEANTE
                System.out.println("CLIENT_SOCKET (created at accept())=" + clientSocket);
                new ServerConnection(clientSocket, server);
            }
        } catch (IOException io) {
            io.printStackTrace();
            return;
        }
    }
}