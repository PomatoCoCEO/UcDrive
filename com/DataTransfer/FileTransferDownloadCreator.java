package com.DataTransfer;

import com.Server.Server;

public class FileTransferDownloadCreator extends Thread {
    private Server server;
    public FileTransferDownloadCreator(Server server) {
        this.server = server;
        this.start();
    }

    public void run() {
        System.out.println("File Transfer tcp creator alive!");
        while(true) {
            try {
                FileTransferDownloadTask ftdt = server.getQueueFileSend().take(); // waits until a new file arrives
                System.out.println("Tirou da queue");


                // ! create new download method
                server.getThreadPoolFiles().execute(new FileDownload(ftdt, server));
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }     
        }
    }
}
