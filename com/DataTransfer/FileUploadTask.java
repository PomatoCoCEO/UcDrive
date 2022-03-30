package com.DataTransfer;

import java.net.ServerSocket;
import java.net.Socket;

import com.Server.conn.ServerConnection;

public class FileUploadTask {
    private ServerConnection serverConnection;
    private ServerSocket serverSocket;
    
    public FileUploadTask(ServerConnection serverConnection, ServerSocket serverSocket) {
        this.serverConnection = serverConnection;
        this.setServerSocket(serverSocket);
    }
    public ServerSocket getServerSocket() {
        return serverSocket;
    }
    public void setServerSocket(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }
    public ServerConnection getServerConnection() {
        return serverConnection;
    }

    public void setServerConnection(ServerConnection serverConnection) {
        this.serverConnection = serverConnection;
    }

    
}
