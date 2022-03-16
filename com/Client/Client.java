package com.Client;

import com.DataTransfer.Reply;
import com.DataTransfer.Request;

import java.io.*;
import java.net.*;
import java.util.Scanner;

import com.User.Credentials;

public class Client {

    public static ObjectOutputStream out;
    public static ObjectInputStream in;
    private static String token;
    private static String serverDir;

    public static void login(){
        while (true) {
                System.out.println("Enter username:");
                String username = commandReader.readLine();
                System.out.println("Enter password:");
                String password = commandReader.readLine();
                Credentials cred = new Credentials(username, password);
                Request login = new Request(cred.loginString());

                out.writeObject(login);
                out.flush();
                
                Reply reply = (Reply) in.readObject();
                switch(reply.getStatusCode()) {
                    case "OK":
                        System.out.println("Login successful");
                        this.token = reply.message;
                        break;
                    case "Unauthorized":
                        System.out.println("Wrong username or password");
                    default:
                        System.out.println("Login failed");                    
                }
            }
    }

    public static void changePassword(){
        Console cons= System.console();
        boolean first = true;
        do {
            if (!first)
                System.out.println("Passwords do not match! Try again");
            System.out.println("Enter new password:");
            String confirmPass = cons.readPassword();
            System.out.println("Confirm password:");
            String newPass = cons.readPassword();
            first = false;
        }
        while(newPass.equals( confirmPass));
        
        Request req = new Request("CH-PASS\n"+ newPass,this.token);
        while (true) {
            out.writeObject(req);
            out.flush();
            Reply reply = (Reply) in.readObject();
            if (reply.getStatusCode().equals("OK")) {
                System.out.println("Password change successful! You have been logged out");
                break;
            } else {
                System.out.println("Password change failed. Trying again...");
            }
        }
        this.token="";
        
        login();
    }
    public static void serverLs(){
        Request req = new Request("LS",this.token);
        while (true) {
            out.writeObject(req);
            out.flush();
            Reply reply = (Reply) in.readObject();
            if (reply.getStatusCode().equals("OK")) {
                System.out.println(reply.getMessage());
                break;
            } else {
                System.out.println("Ls failed. Trying again...");
            }
        }
    }
    public static void changeServerWorkingDirectory(String command){

        String dir = command.split(" ")[1]; // ! check if double spaces

        String[] paths = dir.split("/");

        for( String p: paths){
            if (p.equals("..")){
                int lastSlash = serverDir.lastIndexOf("/");
                if ( lastSlash == -1){
                    System.out.println("Invalid dir");
                    return;
                }
                serverDir= serverDir.substring(0,i);
                
            }
            else{
                serverDir = serverDir + p;
            }
        }


        Request req = new Request("CD\n"+ serverDir ,this.token);
        while (true) {
            out.writeObject(req);
            out.flush();
            Reply reply = (Reply) in.readObject();
            if (reply.getStatusCode().equals("OK")) {
                System.out.println(reply.getMessage());
                break;
            } else {
                System.out.println("Cd failed. Trying again...");
            }
        }
    }

    public static void clientLs(){

    }

    public static boolean handleCommand(String line, BufferedReader commandReader) {
        String[] commands = line.split(" ");
        String command = commands[0];
        switch (command){
            case "exit":
                return true;
                break;
            case "help":
                System.out.println("Available commands:");
                // System.out.println("\tlogin -> Authenticate yourself");
                System.out.println("\tch-pass -> Change your password");
                System.out.println("\tch-server-info -> Change server info");
                System.out.println("\tserver-ls -> Show files in the current server directory");
                System.out.println("\tserver-cd dir_name -> Change server directory to dir_name");
                System.out.println("\tclient-ls -> Show files in the current client directory");
                System.out.println("\tclient-cd dir_name -> Change client directory to dir_name");
                System.out.println("\tdownload file_name -> Download file_name from server");
                System.out.println("\tupload file_name -> Upload file_name to server");
                System.out.println("\texit -> Exit client application");
                System.out.println("\thelp -> Show command list");
                break;
            case "ch-pass":
                changePassword();
                break;
            case "ch-server-info":
                changeServerInfo();
                break;
            case "server-ls":
                serverLs()
                break;
            case "server-cd":
                break;
            case "client-ls":
                break;
            case "client-cd":
                break;
            case "download":
                break;
            case "upload":
                break;
        }
        
        return false;
    }

    public static String primaryServerName, secondaryServerName;
    public static int primaryServerPort, secondaryServerPort;

    public static void readConfig(String fileName) {
        try {

            File fr = new File(fileName);
            Scanner sc = new Scanner(fr);
            // BufferedReader br = new BufferedReader(fr);
            primaryServerName = sc.next();
            primaryServerPort = sc.nextInt();
            secondaryServerName = sc.next();
            secondaryServerPort = sc.nextInt();

        } catch (FileNotFoundException f) {
            System.out.println("File not found: " + f.getMessage());
            f.printStackTrace();
        } catch (IOException e) {
            System.out.println("close:" + e.getMessage());
        }
    }

    

    public static void main(String[] args) {

        // read servers' info from config file

        readConfig("com/Client/config");

        Socket s = null;

        try {
            s = new Socket(primaryServerName, primaryServerPort);

            System.out.println("SOCKET=" + s);
            out = new ObjectOutputStream(s.getOutputStream());
            out.flush();
            in = new ObjectInputStream(s.getInputStream());
            System.out.println("aqui");

            String command = "";
            InputStreamReader input = new InputStreamReader(System.in);
            BufferedReader commandReader = new BufferedReader(input);
            System.out.println("here");

            login();

            System.out.println("Enter command: (help for more info)");
            while (true) {
                try {
                    command = commandReader.readLine();
                    if (handleCommand(command, commandReader))  // true when command == exit
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
        } catch (ClassNotFoundException cnf) {
            System.out.println(cnf.getMessage());
            cnf.printStackTrace();
        } finally {
            if (s != null) {
                try {
                    s.close();
                } catch (IOException e) {
                    System.out.println("close:" + e.getMessage());
                }
            }
        }
    }
}