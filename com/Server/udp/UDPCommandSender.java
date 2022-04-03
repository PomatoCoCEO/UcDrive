package com.Server.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import com.DataTransfer.props.Properties;
import com.Server.Server;

public class UDPCommandSender extends Thread {
    private Server server;
    private String command;

    public UDPCommandSender(Server server, String command) {
        this.server = server;
        this.command = command;
    }

    public void run() {
        try {
            DatagramSocket ds = new DatagramSocket();
            DatagramPacket dp = new DatagramPacket(command.getBytes(), command.length(),
                    server.getDestinationConfig().getServerAddress(),
                    server.getDestinationConfig().getUdpCommandReceiverPort());
            ds.send(dp);
            byte[] response = new byte[Properties.SMALL_MESSAGE_SIZE];
            DatagramPacket resp = new DatagramPacket(response, response.length);
            ds.receive(resp);
            String ans = new String(resp.getData()).trim();
            if (!ans.equals("OK")) {
                System.out.println("Error: " + ans);
            }
            ds.close();
        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
