package com.DataTransfer;

import java.net.InetAddress;
import java.net.Socket;

import com.Server.conn.ServerConnection;

public class FileDownloadTask {

    private String filePath;
    private InetAddress address;
    private int portNo;
    private ServerConnection serverConnection;
	public FileDownloadTask(String filePath, InetAddress address, int portNo, ServerConnection serverConnection) {
        this.filePath = filePath;
        this.address = address;
        this.setPortNo(portNo);
        this.serverConnection = serverConnection;
    }
    public int getPortNo() {
        return portNo;
    }
    public void setPortNo(int portNo) {
        this.portNo = portNo;
    }
    public String getFilePath() {
		return filePath;
	}
	public InetAddress getAddress() {
        return address;
    }
    public void setAddress(InetAddress address) {
        this.address = address;
    }
    public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
    public ServerConnection getServerConnection() {
        return serverConnection;
    }
    public void setServerConnection(ServerConnection serverConnection) {
        this.serverConnection = serverConnection;
    }

    
}
