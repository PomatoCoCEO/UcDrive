package com.DataTransfer;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.nio.file.Paths;

public class UploadThread extends Thread {

    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private long byteSize, noBlocks;
    private String dirPath, fileName;

    private String absolutePath;
    // private String filePath; -> Paths.get(dirPath, fileName)

    private InetAddress destinationAddress;
    private int destinationPort;

    public UploadThread(ObjectInputStream ois, ObjectOutputStream oos, long byteSize, long noBlocks, String dirPath,
            String fileName, String absolutePath, InetAddress destinationAddress, int destinationPort) {
        this.ois = ois;
        this.oos = oos;
        this.byteSize = byteSize;
        this.noBlocks = noBlocks;
        this.dirPath = dirPath;
        this.fileName = fileName;
        this.absolutePath = absolutePath;
        this.destinationAddress = destinationAddress;
        this.destinationPort = destinationPort;
        this.start();
    }

    public void run() {

        try {
            FileTransfer ft = new FileTransfer(ois, oos, byteSize, noBlocks, dirPath, fileName, false);
            ft.join();
            UDPTransfer udpt = new UDPTransfer(byteSize, noBlocks, absolutePath,
                    Paths.get(dirPath, fileName).toString(), true, destinationAddress, destinationPort);
            udpt.join();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}