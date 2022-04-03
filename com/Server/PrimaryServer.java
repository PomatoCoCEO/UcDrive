package com.Server;

import com.DataTransfer.creators.FileTransferDownloadCreator;
import com.DataTransfer.creators.FileTransferUdpCreator;
import com.DataTransfer.creators.FileTransferUploadCreator;
import com.Server.auth.*;
import com.Server.config.ConfigServer;
import com.Server.conn.ServerConnection;
import com.Server.heartbeats.PrimaryHeartbeat;
import com.Server.heartbeats.SecondaryHeartbeat;
import com.Server.udp.UDPAccept;
import com.Server.udp.UDPCommandReceiver;

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

    public PrimaryServer(String ownConfigFile, String otherConfigFile, String absPath, String configPath) {
        super(ownConfigFile, otherConfigFile, absPath, configPath);
    }

    public void work() {
        try {
            ConfigServer secondaryServerConfig = otherConfig;
            DatagramSocket ds = new DatagramSocket(ownConfig.getUdpHeartbeatPort());
            // ! here the port must be the one in the configuration
            ds.setSoTimeout(SOCKET_TIMEOUT_MILLISECONDS);
            byte[] buffer = new byte[1000];
            boolean isCurrentlyPrimary = true;
            while (true) {
                DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
                try {
                    ds.receive(reply); // receives in the specified socket

                    String rr = (new String(reply.getData())).trim();
                    if (rr.equals("I AM PRIMARY")) {

                        // act like a secondary server
                        isCurrentlyPrimary = false;
                        System.out.println("Sou secundario");
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
                    break;
                }
            }

            if (isCurrentlyPrimary) {
                PrimaryHeartbeat phb = new PrimaryHeartbeat(ds, secondaryServerConfig, false);
                bePrimary();
                phb.join();
                // acceptTcp();
            } else {

                try {

                    ds.setSoTimeout(SOCKET_TIMEOUT_MILLISECONDS);
                    SecondaryHeartbeat sh = new SecondaryHeartbeat(ds, secondaryServerConfig);
                    UDPAccept udpAccept = new UDPAccept(this); // we need to use our config
                    UDPCommandReceiver ucr = new UDPCommandReceiver(this);
                    // here we should make another thread to accept the dup connections
                    sh.join();
                    System.out.println("Pserver Secondary heartbeat joined.");
                    udpAccept.interrupt();
                    ucr.interrupt();
                    // after the server knows it will replace the primary server
                    System.out.println("UDP transfer thread interrupted.");
                    // it should kill the file receipt thread

                    // ! udpAccept won't die, so we need to fix this
                    System.out.println("UDP transfer thread over.");
                    PrimaryHeartbeat ph = new PrimaryHeartbeat(ds, secondaryServerConfig, true);
                    bePrimary();
                    ph.join();
                    // ! we might need to check the value of isSecondary in this call
                    // ds.close();
                } catch (SocketException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

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
        String configPath = System.getProperty("user.dir") + "/com/Server/primary/runfiles";

        PrimaryServer ps = new PrimaryServer("Pconfig", "Sconfig", absPath, configPath);
        // one configuration is relative to the server itself,
        // the other is relative to the secondary server
        ps.work();

        // read single heartbeat
        // if I AM PRIMARY then create receive files thread and send heartbeats to
        // primary
        // else listen to TCP connections and create HB thread to reply to SECONDARY

    }

}
