package com.Server.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

public class ConfigServer {
    private int tcpSocketPort;
    private int udpFileTransferPort;
    private int udpHeartbeatPort;
    private int udpCommandReceiverPort;
    private InetAddress serverAddress;

    public ConfigServer(String fileName) {
        File fr = new File(fileName);
        try (Scanner sc = new Scanner(fr)) {
            // BufferedReader br = new BufferedReader(fr);
            this.serverAddress = InetAddress.getByName(sc.nextLine());
            this.setTcpSocketPort(Integer.parseInt(sc.nextLine()));
            this.setUdpFileTransferPort(Integer.parseInt(sc.nextLine()));
            this.setUdpHeartbeatPort(Integer.parseInt(sc.nextLine()));
            this.setUdpCommandReceiverPort(Integer.parseInt(sc.nextLine()));
        } catch (FileNotFoundException f) {
            System.out.println("File not found: " + f.getMessage());
            f.printStackTrace();
        } catch (UnknownHostException uhe) {
            System.out.println("Problems in host address determination: " + uhe.getMessage());
            uhe.printStackTrace();
        }
    }

    public int getUdpCommandReceiverPort() {
        return udpCommandReceiverPort;
    }

    public void setUdpCommandReceiverPort(int udpCommandReceiverPort) {
        this.udpCommandReceiverPort = udpCommandReceiverPort;
    }

    public InetAddress getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(InetAddress serverAddress) {
        this.serverAddress = serverAddress;
    }

    public int getTcpSocketPort() {
        return tcpSocketPort;
    }

    public void setTcpSocketPort(int tcpSocketPort) {
        this.tcpSocketPort = tcpSocketPort;
    }

    public int getUdpFileTransferPort() {
        return udpFileTransferPort;
    }

    public void setUdpFileTransferPort(int udpFileTransferPort) {
        this.udpFileTransferPort = udpFileTransferPort;
    }

    public int getUdpHeartbeatPort() {
        return udpHeartbeatPort;
    }

    public void setUdpHeartbeatPort(int udpHeartbeatPort) {
        this.udpHeartbeatPort = udpHeartbeatPort;
    }

    public String toString() {
        return "Config: ip address " + serverAddress.toString() + ";\nudp file port " + udpFileTransferPort +
                ";\nudp heartbeat port " + udpHeartbeatPort + ";\ntcp socket port " + tcpSocketPort;
    }

}
