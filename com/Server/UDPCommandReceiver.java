package com.Server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.DataTransfer.props.Properties;

public class UDPCommandReceiver extends Thread {

    private Server server;
    private final static int PACKET_LENGTH = Properties.SMALL_MESSAGE_SIZE;
    private final static int THREADS_PER_POOL = 10;

    public UDPCommandReceiver(Server server) {
        this.server = server;
        this.start();
    }

    public void run() {
        ExecutorService threadPoolReceivers = Executors.newFixedThreadPool(THREADS_PER_POOL);
        try {
            DatagramSocket ds = new DatagramSocket(server.getConfig().getUdpCommandReceiverPort());
            while (true) {
                byte[] message = new byte[PACKET_LENGTH];
                DatagramPacket dp = new DatagramPacket(message, message.length);
                ds.receive(dp);
                threadPoolReceivers.execute(new UDPCommandHandler(ds, dp, server));
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}
