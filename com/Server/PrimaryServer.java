package com.Server;

import com.DataTransfer.FileTransferDownloadCreator;
import com.DataTransfer.FileTransferUdpCreator;
import com.DataTransfer.FileTransferUploadCreator;
import com.Server.auth.*;
import com.Server.config.ConfigServer;
import com.Server.conn.ServerConnection;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.concurrent.Executors;

public class PrimaryServer extends Server {

    public PrimaryServer(String ownConfigFile, String otherConfigFile, String absPath) {
        super(ownConfigFile, otherConfigFile, absPath);
    }

    public void work() {
        try {
            ConfigServer secondaryServerConfig = new ConfigServer("com/Server/runfiles/Sconfig");
            DatagramSocket ds = new DatagramSocket(ownConfig.getUdpHeartbeatPort());
            // ! here the port must be the one in the configuration
            ds.setSoTimeout(SOCKET_TIMEOUT_MILLISECONDS);
            byte[] buffer = new byte[1000];
            boolean isCurrentlyPrimary = true;
            while (true) {
                DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
                try {
                    ds.receive(reply); // receives in the specified socket
                    if ((new String(reply.getData())).equals("I AM PRIMARY")) {
                        // act like a secondary server
                        isCurrentlyPrimary = false;
                        new SecondaryHeartbeat(ds, secondaryServerConfig);
                        // upd file update
                        break;
                    } else {
                        // act like a primary server
                        // PrimaryHeartbeat phb = new PrimaryHeartbeat(ds, secondaryServerConfig
                        // ,false);
                        // go to tcp
                        break;
                    }
                } catch (SocketTimeoutException e) {
                    // TODO Auto-generated catch block
                    System.out.println("No response from secondary server");
                    // ! become primary
                    break;
                } catch (IOException io) {
                    System.out.println("Problems: " + io.getMessage());
                    io.printStackTrace();
                }
            }

            if (isCurrentlyPrimary) {
                PrimaryHeartbeat phb = new PrimaryHeartbeat(ds, secondaryServerConfig, false);
                bePrimary();
                phb.join();
                // acceptTcp();
            } else {
                UDPAccept ua = new UDPAccept(this);
                ua.join();
            }
        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String absPath = System.getProperty("user.dir") + "/com/Server/primary/data";

        PrimaryServer ps = new PrimaryServer("Pconfig", "Sconfig", absPath);
        // one configuration is relative to the server itself,
        // the other is relative to the secondary server
        ps.work();

        // read single heartbeat
        // if I AM PRIMARY then create receive files thread and send heartbeats to
        // primary
        // else listen to TCP connections and create HB thread to reply to SECONDARY

    }

}
