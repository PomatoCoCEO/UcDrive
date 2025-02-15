package com.DataTransfer.creators;

import com.DataTransfer.tasks.FileDownloadTask;
import com.DataTransfer.threads.ServerDownload;
import com.Server.Server;

public class FileTransferDownloadCreator extends Thread {
    private Server server;
    public FileTransferDownloadCreator(Server server) {
        this.server = server;
        this.start();
    }

    public void run() {
        while(true) {
            try {
                FileDownloadTask fdt = server.getQueueFileSend().take(); // waits until a new file arrives
                // ! create new download method
                server.getThreadPoolFiles().execute(new ServerDownload(fdt));
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }     
        }
    }
}
