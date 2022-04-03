package com.Server.heartbeats;

import java.net.DatagramSocket;

import com.Server.heartbeats.Heartbeat;
import com.Server.config.ConfigServer;

public class SecondaryHeartbeat extends Heartbeat {
    public SecondaryHeartbeat(DatagramSocket ds, ConfigServer cs) {

        super(ds, cs);
        this.primary = false;
        System.out.printf("Secondary heartbeat: ip %s , port %d\n", cs.getServerAddress(), cs.getUdpHeartbeatPort());
        this.start();
    }

    public void run() {
        secondaryHeartbeat();
        /*
         * boolean sec_before = false;
         * while (true) {
         * if (primary) {
         * primary = primaryHeartbeat(sec_before);
         * } else {
         * primary = secondaryHeartbeat();
         * sec_before = true;
         * 
         * }
         * }
         */
    }

}