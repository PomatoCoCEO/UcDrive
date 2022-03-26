package com.Client;

import com.Client.config.ConfigClient;
import com.Client.conn.ClientConnection;
import com.DataTransfer.Reply;
import com.DataTransfer.Request;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.io.*;
import java.io.ObjectInputFilter.Config;
import java.net.*;
import java.util.Scanner;

import com.User.Credentials;

public class Client {

    private static String token;
    private static String serverDir;
    private static String clientDir;

    public static void main(String[] args) {

        // read servers' info from config file

        ConfigClient config = new ConfigClient("com/Client/config");

        Socket s = null;

        try {
            s = new Socket(config.getPrimaryServerName(), config.getPrimaryServerPort());
            ClientConnection clientConnection = new ClientConnection(s);

            System.out.println("SOCKET=" + s);

            String command = "";
            InputStreamReader input = new InputStreamReader(System.in);
            BufferedReader commandReader = new BufferedReader(input);

            CommandHandler commandHandler = new CommandHandler(clientConnection, s);
            // System.out.println("here");

            commandHandler.login(commandReader);

            System.out.println("Enter command: (help for more info)");
            while (true) {
                try {
                    command = commandReader.readLine().trim();
                    if (commandHandler.handleCommand(command, commandReader)) // true when command == exit
                        break;

                } catch (Exception e) {
                    System.out.println("message:" + e.getMessage());
                }
            }

            System.out.println("Exiting client");

        } catch (UnknownHostException e) {
            System.out.println("Sock:" + e.getMessage());

        } catch (IOException io) {
            System.out.println(io.getMessage());
            io.printStackTrace();
        }
        // catch (ClassNotFoundException cnf) {
        // System.out.println(cnf.getMessage());
        // cnf.printStackTrace();
        // }
        finally {
            if (s != null) {
                try {
                    s.close();
                } catch (IOException e) {
                    System.out.println("close:" + e.getMessage());
                }
            }
        }
    }

    public static String getClientDir() {
        return clientDir;
    }

    public static void setClientDir(String clientDir) {
        Client.clientDir = clientDir;
    }

    public static String getServerDir() {
        return serverDir;
    }

    public static void setServerDir(String serverDir) {
        Client.serverDir = serverDir;
    }

    public static String getToken() {
        return token;
    }

    public static void setToken(String token) {
        Client.token = token;
    }
}