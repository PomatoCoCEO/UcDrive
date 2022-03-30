package com.DataTransfer;

import java.net.Socket;

import com.Server.conn.ServerConnection;

public class FileUploadTask {
    private ServerConnection serverConnection;
    
    public FileUploadTask(ServerConnection serverConnection) {
        this.serverConnection = serverConnection;
    }
    public ServerConnection getServerConnection() {
        return serverConnection;
    }

    public void setServerConnection(ServerConnection serverConnection) {
        this.serverConnection = serverConnection;
    }

    
}
