package com.DataTransfer.creators;

import com.DataTransfer.tasks.UDPFileTransferTask;
import com.DataTransfer.threads.UDPTransfer;
import com.Server.Server;

public class FileTransferUdpCreator extends Thread {
    private Server server;
    public FileTransferUdpCreator(Server server) {
        this.server = server;
        this.start();
    }

    public void run() {
        while(true) {
            try {
                UDPFileTransferTask uftt = server.getQueueUdp().take(); // waits until a new file arrives at the queue
                System.out.println("let us take the element from the queue");
                server.getThreadPoolFiles().execute(new UDPTransfer(uftt));
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }     
        }
    }
}