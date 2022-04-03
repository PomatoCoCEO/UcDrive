package com.Server;

import com.Server.config.ConfigServer;
import com.Server.heartbeats.PrimaryHeartbeat;
import com.Server.heartbeats.SecondaryHeartbeat;
import com.Server.udp.UDPAccept;
import com.Server.udp.UDPCommandReceiver;

import java.net.DatagramSocket;
import java.net.SocketException;

public class SecondaryServer extends Server {

    public SecondaryServer(String ownConfigFile, String otherConfigFile, String absPath, String configPath) {
        super(ownConfigFile, otherConfigFile, absPath, configPath);
    }

    public void work() {

        try {
            ConfigServer primaryServerConfig = getDestinationConfig();
            // new ConfigServer("com/Server/runfiles/Pconfig");

            DatagramSocket ds = new DatagramSocket(ownConfig.getUdpHeartbeatPort());
            // ! this socket should also be determined statically
            ds.setSoTimeout(SOCKET_TIMEOUT_MILLISECONDS);
            SecondaryHeartbeat sh = new SecondaryHeartbeat(ds, primaryServerConfig);
            UDPAccept udpAccept = new UDPAccept(this); // we need to use our config
            UDPCommandReceiver ucr = new UDPCommandReceiver(this);
            // here we should make another thread to accept the dup connections
            sh.join();
            System.out.println("Secondary heartbeat joined.");
            udpAccept.interrupt();
            ucr.interrupt();
            // after the server knows it will replace the primary server
            System.out.println("UDP transfer thread interrupted.");
            // it should kill the file receipt thread

            // ! udpAccept won't die, so we need to fix this
            System.out.println("UDP transfer thread over.");
            PrimaryHeartbeat ph = new PrimaryHeartbeat(ds, primaryServerConfig, true);
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

        // send I AM SECONDARY
        // create heartbeat thread
        // create receive files thread that shuts down if server becomes primary
        // if PRIMARY does not respond 5 times to heartbeat, become primary and start
        // sending I AM PRIMARY
        // and start listening to TCP connections
    }

    public static void main(String[] args) {
        String absPath = System.getProperty("user.dir") + "/com/Server/secondary/data";
        String configPath = System.getProperty("user.dir") + "/com/Server/secondary/runfiles";
        SecondaryServer ss = new SecondaryServer("Sconfig", "Pconfig", absPath, configPath);
        ss.work();

    }

}
