package com.Server;

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

public class PrimaryServer extends Server {

    public PrimaryServer(String configFile) {
        super(configFile);
    }

    public void work() {
        absolutePath = System.getProperty("user.dir") + "/com/Server/primary/data";
        try {
            ConfigServer secondaryServerConfig = new ConfigServer("com/Server/runfiles/Sconfig");
            DatagramSocket ds = new DatagramSocket(config.getUdpHeartbeatPort()); 
            //! here the port must be the one in the configuration
            ds.setSoTimeout(SOCKET_TIMEOUT_MILLISECONDS);
            byte[] buffer = new byte[1000];
            boolean isCurrentlyPrimary = true;
            while (true) {
                DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
                try {
                    ds.receive(reply); // receives in the specified socket
                    if ((new String(reply.getData())).equals("I AM PRIMARY")) {
                        // act like a secondary server
                        new SecondaryHeartbeat(ds, secondaryServerConfig);
                        // upd file update

                        break;
                    } else {
                        // act like a primary server
                        PrimaryHeartbeat phb = new PrimaryHeartbeat(ds, secondaryServerConfig ,false);
                        // go to tcp
                        break;
                    }
                } catch (SocketTimeoutException e) {
                    // TODO Auto-generated catch block
                    System.out.println("Waiting for config from secondary server");
                    continue;
                } catch (IOException io) {
                    System.out.println("Problems: " + io.getMessage());
                    io.printStackTrace();
                }
            }
            if (isCurrentlyPrimary) {
                TCPAccept ta = new TCPAccept(this);
                ta.join();
                // acceptTcp();
            }
            else {
                UDPAccept ua = new UDPAccept(this);
                ua.join();
                // acceptUdp();
            }
            // ds.close();
        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        PrimaryServer ps = new PrimaryServer("Pconfig");
        ps.work();

        // read single heartbeat
        // if I AM PRIMARY then create receive files thread and send heartbeats to
        // primary
        // else listen to TCP connections and create HB thread to reply to SECONDARY

    }

}
