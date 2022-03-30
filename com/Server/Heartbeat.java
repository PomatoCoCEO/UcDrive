package com.Server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;

import com.Server.config.ConfigServer;

public class Heartbeat extends Thread {
    private DatagramSocket ds;
    private ConfigServer cs;
    public final static int ALLOWED_HEARTBEAT_FAILURES = 3;
    protected final static int HEARTBEAT_SLEEP_MILLISECONDS = 500;
    protected boolean primary = true;

    /**
     * Class constructor
     * @param ds the origin socket, where the heartbeat is sent from
     * @param cs the primary server configuration, with the ip and port for communication
     */
    public Heartbeat(DatagramSocket ds, ConfigServer cs) {
        this.cs = cs;
        this.ds = ds;
    }

    public boolean secondaryHeartbeat() {
        // send I AM SECONDARY
        int noFailedHeartbeats = 0;
        String message = "I AM SECONDARY";
        while (noFailedHeartbeats < ALLOWED_HEARTBEAT_FAILURES) {
            DatagramPacket request = new DatagramPacket(message.getBytes(), message.length(), cs.getServerAddress(),
                    cs.getUdpHeartbeatPort());
            System.out.println("Secondary server heartbeat");
            System.out.println("Sending datagram to port "+request.getPort()+
                    ", address "+request.getAddress()+", content "+request.getData());
            System.out.println("Original port is "+ds.getLocalPort());
            try {
                ds.send(request);
                byte[] buffer = new byte[1000];
                DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
                ds.receive(reply);
                noFailedHeartbeats = 0;
                /*if ((new String(reply.getData())).equals("I AM PRIMARY")) {
                    noFailedHeartbeats = 0;
                }*/
                Thread.sleep(HEARTBEAT_SLEEP_MILLISECONDS);
            } catch (SocketTimeoutException ste) {
                noFailedHeartbeats++;
                System.out.println("Heartbeats failed: "+noFailedHeartbeats);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
        // close udp file transfer by id
        // lauch tcp app
        return true;

    }

    public boolean primaryHeartbeat(boolean isSecondary) {
        String message = "I AM PRIMARY";
        boolean secondaryActivity = false;
        int noFailedHeartbeats = 0;

        while (!secondaryActivity || noFailedHeartbeats < ALLOWED_HEARTBEAT_FAILURES) {
            DatagramPacket request = new DatagramPacket(message.getBytes(), message.length(), cs.getServerAddress(),
                    cs.getUdpHeartbeatPort());
            try {
                System.out.println("Primary heartbeat sending datagram to port "+request.getPort()+", address "+request.getAddress()+", content "+request.getData());
                ds.send(request);
                byte[] buffer = new byte[1000]; // ! this is a magic number
                DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
                ds.receive(reply);
                noFailedHeartbeats = 0;
                if ((new String(reply.getData())).equals("I AM PRIMARY") && !isSecondary && !secondaryActivity) {
                    message = "I AM SECONDARY";
                    secondaryActivity = true;
                    return false;
                    // create Receive udp files
                }
                Thread.sleep(HEARTBEAT_SLEEP_MILLISECONDS);
            } catch (SocketTimeoutException ste) {
                if (isSecondary)
                    System.out.println("Primary server not restored yet");
                /*else
                    System.out.println("Secondary server down");*/
                noFailedHeartbeats++;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InterruptedException e) {
                // thread interruption while sleeping
                e.printStackTrace();
            }

        }

        // create Receive udp files
        return false;
    }

    public DatagramSocket getDs() {
        return ds;
    }

    public void setDs(DatagramSocket ds) {
        this.ds = ds;
    }

    public void run() {}

}
