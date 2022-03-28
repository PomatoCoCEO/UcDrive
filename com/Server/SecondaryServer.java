package com.Server;

import com.Server.auth.*;
import com.Server.config.ConfigServer;
import com.Server.conn.ServerConnection;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SecondaryServer extends Server {

    public SecondaryServer(String configFile) {
        super(configFile);
    }

    public void work() {

        absolutePath = System.getProperty("user.dir") + "/com/Server/secondary/data";

        try {
            ConfigServer primaryServerConfig = new ConfigServer("com/Server/runfiles/Pconfig");
            
            DatagramSocket ds = new DatagramSocket(config.getUdpHeartbeatPort());
            //! this socket should also be determined statically
            ds.setSoTimeout(SOCKET_TIMEOUT_MILLISECONDS);
            SecondaryHeartbeat sh = new SecondaryHeartbeat(ds, primaryServerConfig);
            UDPAccept udpAccept = new UDPAccept(this); // we need to use our config
            // here we should make another thread to accept the dup connections
            sh.join();
            udpAccept.interrupt(); 
            // after the server knows it will replace the primary server 
            // it should kill the file receipt thread
            udpAccept.join();
            PrimaryHeartbeat ph = new PrimaryHeartbeat(ds, primaryServerConfig, true); 
            TCPAccept ta = new TCPAccept(this);
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

        // send I AM SECONDARY
        // create heartbeat thread
        // create receive files thread that shuts down if server becomes primary
        // if PRIMARY does not respond 5 times to heartbeat, become primary and start
        // sending I AM PRIMARY
        // and start listening to TCP connections
    }

    public static void main(String[] args) {
        SecondaryServer ss = new SecondaryServer("Sconfig");
        ss.work();

    }

}
