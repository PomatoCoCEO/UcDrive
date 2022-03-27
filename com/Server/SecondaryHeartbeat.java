package com.Server;

import java.net.DatagramSocket;

import com.Server.config.ConfigServer;

public class SecondaryHeartbeat extends Heartbeat {
    public SecondaryHeartbeat(DatagramSocket ds, ConfigServer cs) {
        super(ds, cs);
        this.primary = false;
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