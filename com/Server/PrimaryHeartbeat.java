package com.Server;

import java.net.DatagramSocket;

import com.Server.config.ConfigServer;

public class PrimaryHeartbeat extends Heartbeat {
    public PrimaryHeartbeat(DatagramSocket ds, ConfigServer cs) {
        super(ds, cs);
        this.start();

    }

    public void run() {
        boolean sec_before = false;
        while (true) {
            if (primary) {
                primary = primaryHeartbeat(sec_before);
            } else {
                primary = secondaryHeartbeat();
                sec_before = true;

            }
        }
    }
}