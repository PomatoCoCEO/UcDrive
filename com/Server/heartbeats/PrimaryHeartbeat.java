package com.Server.heartbeats;

import java.net.DatagramSocket;
import com.Server.Heartbeat;

import com.Server.config.ConfigServer;

public class PrimaryHeartbeat extends Heartbeat {
    private boolean isSecondary;
    
    /**
     * Class constructor
     * @param ds origin socket
     * @param cs configuration for the secondary server
     * @param isSecondary if the server is primary but is actually acting as secondary
     */
    
    public PrimaryHeartbeat(DatagramSocket ds, ConfigServer cs, boolean isSecondary) {
        super(ds, cs);
        this.isSecondary = false;
        this.start();
    }

    public void run() {
        primaryHeartbeat(isSecondary);
    }
}