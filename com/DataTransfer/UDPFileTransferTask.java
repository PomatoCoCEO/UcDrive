package com.DataTransfer;

import java.net.InetAddress;

import com.Server.Server;

public class UDPFileTransferTask {

    private long byteSize;
    private long noBlocks; 
    private String absolutePath; 
    private String filePath; 
    private boolean send;
    private InetAddress destinationAddress;
    private int destinationPort;
    private Server server;
    
    public UDPFileTransferTask(long byteSize, long noBlocks, String absolutePath, String filePath, boolean send,
            InetAddress destinationAddress, int destinationPort) {
        this.byteSize = byteSize;
        this.noBlocks = noBlocks;
        this.absolutePath = absolutePath;
        this.filePath = filePath;
        this.send = send;
        this.destinationAddress = destinationAddress;
        this.destinationPort = destinationPort;
    }
    public long getByteSize() {
        return byteSize;
    }
    public void setByteSize(long byteSize) {
        this.byteSize = byteSize;
    }
    public long getNoBlocks() {
        return noBlocks;
    }
    public void setNoBlocks(long noBlocks) {
        this.noBlocks = noBlocks;
    }
    public String getAbsolutePath() {
        return absolutePath;
    }
    public void setAbsolutePath(String absolutePath) {
        this.absolutePath = absolutePath;
    }
    public String getFilePath() {
        return filePath;
    }
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    public boolean isSend() {
        return send;
    }
    public void setSend(boolean send) {
        this.send = send;
    }
    public InetAddress getDestinationAddress() {
        return destinationAddress;
    }
    public void setDestinationAddress(InetAddress destinationAddress) {
        this.destinationAddress = destinationAddress;
    }
    public int getDestinationPort() {
        return destinationPort;
    }
    public void setDestinationPort(int destinationPort) {
        this.destinationPort = destinationPort;
    }
}
