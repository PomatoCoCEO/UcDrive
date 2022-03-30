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
    private static ConfigClient config;
    private static boolean connectedToPrimary = true;
    public static final int CLIENT_SOCKET_TIMEOUT_MILLISECONDS = 2000;

    public static CommandHandler switchServer (CommandHandler handler, ClientConnection cc) {
        // tries to connect to the other server and closes the current connection
        try {
            InetAddress prim = InetAddress.getByName(config.getPrimaryServerName());
            InetAddress sec = InetAddress.getByName(config.getSecondaryServerName());
            InetAddress nextTry;
            int prt;
            if( connectedToPrimary) {
                nextTry = sec;
                prt=config.getSecondaryServerPort();
            }
            else {
                nextTry = prim;
                prt = config.getPrimaryServerPort();
            }
            Socket sock = new Socket(nextTry, prt);
            sock.setSoTimeout(Client.CLIENT_SOCKET_TIMEOUT_MILLISECONDS);
            ObjectOutputStream oos = new ObjectOutputStream(sock.getOutputStream());
            oos.flush();
            ObjectInputStream ois = new ObjectInputStream(sock.getInputStream());
            cc.setSocketParams(sock, ois, oos);
            CommandHandler newHandler = new CommandHandler(cc, sock, nextTry, prt, config);
            System.out.println("Is socket closed? "+sock.isClosed());
            connectedToPrimary = !connectedToPrimary;
            System.out.println("Enter your credentials again");
            newHandler.login(new BufferedReader(new InputStreamReader(System.in)));
            
            return newHandler;
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch(IOException io ){
            System.out.println("Server switch unsuccessful: "+io.getMessage());
        }
        return new CommandHandler(); // invalid
    }

    public static void main(String[] args) {

        // read servers' info from config file

        config = new ConfigClient("com/Client/config");

        CommandHandler commandHandler = new CommandHandler();
        ClientConnection clientConnection= new ClientConnection();
        Socket s = null;
        System.out.println("null init done");

        try {
            System.out.println("Trying to connect to "+config.getPrimaryServerName()+
                " with port "+config.getPrimaryServerPort());
            s = new Socket(config.getPrimaryServerName(), config.getPrimaryServerPort());
            System.out.println("socket created");

            s.setSoTimeout(CLIENT_SOCKET_TIMEOUT_MILLISECONDS);
            clientConnection = new ClientConnection(s,config);

            System.out.println("SOCKET=" + s);

            String command = "";
            InputStreamReader input = new InputStreamReader(System.in);
            BufferedReader commandReader = new BufferedReader(input);

            InetAddress add = InetAddress.getByName(config.getPrimaryServerName());

            commandHandler = new CommandHandler(clientConnection, s, add, config.getPrimaryServerPort(), config);
            System.out.println("here");

            commandHandler.login(commandReader);

            System.out.println("Enter command: (help for more info)");
            while (true) {
                try {
                    System.out.print(serverDir+"$ ");
                    command = commandReader.readLine().trim();
                    if (commandHandler.handleCommand(command, commandReader)) // true when command == exit
                        break;

                }catch (SocketTimeoutException | SocketException e){
                    System.out.println("Switch 1");
                    commandHandler= switchServer(commandHandler, clientConnection);
                } catch (Exception e) {
                    System.out.println("message:" + e.getMessage());
                }
            }

            System.out.println("Exiting client");

        } catch (UnknownHostException e) {
            System.out.println("Sock:" + e.getMessage());

        }catch(SocketException e) {
            System.out.println("Switch 2: "+ e.getMessage());
            commandHandler= switchServer(commandHandler, clientConnection);
        } catch (IOException io) {
            System.out.println(io.getMessage());
            io.printStackTrace();
        }
        
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