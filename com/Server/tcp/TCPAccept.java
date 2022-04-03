package com.Server.tcp;

import java.io.IOException;
import java.net.Socket;

import com.Server.Server;
import com.Server.conn.ServerConnection;

public class TCPAccept extends Thread {
    private Server server;

    public TCPAccept(Server server) {
        this.server = server;
        this.start();
    }

    public void run() {
        acceptTcp();
    }

    private void acceptTcp() {

        while (true) {
            // System.out.println("Estou a aceitar clientes");
            try {
                Socket clientSocket = server.getServerSocket().accept();
                System.out.println("CLIENT_SOCKET (created at accept())=" + clientSocket);
                server.getThreadPoolTcpAccept().execute(new ServerConnection(clientSocket, server));

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}