package com.DataTransfer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Paths;

import com.Server.Server;

public class ServerFileTransfer extends FileTransfer {
    private Server server;
    public ServerFileTransfer (FileTransferTask ftt, Server server) {
        try {
            this.oos = new ObjectOutputStream(ftt.getSocket().getOutputStream());
            oos.flush();
            this.ois = new ObjectInputStream(ftt.getSocket().getInputStream());
            this.byteSize = ftt.getByteSize();
            this.noBlocks = ftt.getNumberOfBlocks();
            this.dirPath = ftt.getDirPath();
            this.fileName = ftt.getFileName();
            this.send = ftt.isDownload();
            this.server= server;
            this.start();
        } catch(IOException io) {
            io.printStackTrace();
        }
    }

    public void receiveFile() {
        super.receiveFile();
        String fileNameWithoutDirectory = fileName.substring(fileName.lastIndexOf('/') + 1);
        String filePath = Paths.get(dirPath, fileNameWithoutDirectory).toString();
        UDPFileTransferTask uftt = new UDPFileTransferTask(byteSize, noBlocks,
                    server.getAbsolutePath(), filePath, true, server.getDestinationConfig().getServerAddress(),
                    server.getDestinationConfig().getUdpFileTransferPort());
        server.getQueueUdp().add(uftt);
    }
}
