package com.Server;

import java.net.DatagramSocket;
import com.Server.Heartbeat;

import com.Server.config.ConfigServer;

public class PrimaryHeartbeat extends Heartbeat {
    private boolean isSecondary;
    public PrimaryHeartbeat(DatagramSocket ds, ConfigServer cs, boolean isSecondary) {
        super(ds, cs);
        this.isSecondary = false;
        this.start();
    }

    public void run() {
        primaryHeartbeat(isSecondary);
    }
}