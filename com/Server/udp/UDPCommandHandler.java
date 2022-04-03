package com.Server.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import com.Server.Server;
import com.Server.auth.User;
import com.Server.auth.Auth.Operation;

public class UDPCommandHandler extends Thread {
    private DatagramSocket ds;
    private DatagramPacket dp;
    private Server server;

    public UDPCommandHandler(DatagramSocket ds, DatagramPacket dp, Server server) {
        this.ds = ds;
        this.dp = dp;
        this.server = server;
    }

    private void sendResponse(String response) {
        DatagramPacket toSend = new DatagramPacket(response.getBytes(), response.length(), dp.getAddress(),
                dp.getPort());
        try {
            ds.send(toSend);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void run() {
        String command = new String(dp.getData()).trim();
        String[] sp = command.split("\n");
        if (sp.length < 3)
            sendResponse("ERROR");
        else {
            String comm = sp[0];
            String usr = sp[1];
            String newState = sp[2];
            handleCommand(comm, usr, newState);
        }
    }

    public void changeUserPassword(String user, String newPassword) {
        User newUser = server.getAuthInfo().findUser(user);
        if (newUser == null) {
            sendResponse("ERROR NO SUCH USER");
            return;
        }
        newUser.setPasswordHash(newPassword);
        server.getAuthInfo().changeUsers(Operation.CHANGE, newUser);
        sendResponse("OK");
    }

    public void changeUserDirectory(String username, String newDirectory) {
        User newUser = server.getAuthInfo().findUser(username);
        if (newUser == null) {
            sendResponse("ERROR NO SUCH USER");
            return;
        }
        newUser.setServerDir(newDirectory);

        server.getAuthInfo().changeUsers(Operation.CHANGE, newUser);
        User newUser2 = server.getAuthInfo().findUser(username);
        System.out.println("New user info: " + newUser2);
        sendResponse("OK");
    }

    public void handleCommand(String comm, String usr, String newState) {
        switch (comm) {
            case "CH-PASS":
                changeUserPassword(usr, newState);
                break;
            case "SCD":
                changeUserDirectory(usr, newState);
                break;
            default:
                System.out.println("Invalid command: " + comm);
                sendResponse("ERROR INVALID COMMAND");
        }
    }

}
