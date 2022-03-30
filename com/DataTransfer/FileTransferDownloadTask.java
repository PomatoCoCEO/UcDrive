package com.DataTransfer;

import java.net.Socket;

public class FileTransferDownloadTask {

    private Socket socket;
    private String dirPath;
    private String fileName;
    private long byteSize;
    private long numberOfBlocks;

    public FileTransferDownloadTask( Socket socket, String dirPath, String fileName, long byteSize,
            long numberOfBlocks) {
        this.socket = socket;
        this.dirPath = dirPath;
        this.fileName = fileName;
        this.byteSize = byteSize;
        this.numberOfBlocks = numberOfBlocks;
    }

    public Socket getSocket() {
        return socket;
    }
    public void setSocket(Socket socket) {
        this.socket = socket;
    }
    public String getDirPath() {
        return dirPath;
    }
    public void setDirPath(String dirPath) {
        this.dirPath = dirPath;
    }
    public String getFileName() {
        return fileName;
    }
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    public long getByteSize() {
        return byteSize;
    }
    public void setByteSize(long byteSize) {
        this.byteSize = byteSize;
    }
    public long getNumberOfBlocks() {
        return numberOfBlocks;
    }
    public void setNumberOfBlocks(int numberOfBlocks) {
        this.numberOfBlocks = numberOfBlocks;
    }
    
}
