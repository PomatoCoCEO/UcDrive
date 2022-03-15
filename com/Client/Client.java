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

    public static boolean handle_command(String line, BufferedReader command_reader) {
        String[] commands = line.split(" ");
        String command = commands[0];
        if (command.equals("exit"))
            return true;
        else if (command.equals("help")) {
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
        } else if (command.equals("ch-pass")) {

        } else if (command.equals("ch-server-info")) {

        } else if (command.equals("server-ls")) {

        } else if (command.equals("client-ls")) {

        } else if (command.equals("server-cd")) {

        } else if (command.equals("client-cd")) {

        } else if (command.equals("download")) {

        } else if (command.equals("upload")) {

        }
        return false;
    }

    public static String primary_server_name, secondary_server_name;
    public static int primary_server_port, secondary_server_port;

    public static void readConfig(String fileName) {
        try {

            File fr = new File(fileName);
            Scanner sc = new Scanner(fr);
            // BufferedReader br = new BufferedReader(fr);
            primary_server_name = sc.next();
            primary_server_port = sc.nextInt();
            secondary_server_name = sc.next();
            secondary_server_port = sc.nextInt();

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
            s = new Socket(primary_server_name, primary_server_port);

            System.out.println("SOCKET=" + s);
            out = new ObjectOutputStream(s.getOutputStream());
            out.flush();
            in = new ObjectInputStream(s.getInputStream());
            System.out.println("aqui");

            String command = "";
            InputStreamReader input = new InputStreamReader(System.in);
            BufferedReader command_reader = new BufferedReader(input);
            System.out.println("here");

            while (true) {
                System.out.println("Enter username:");
                String username = command_reader.readLine();
                System.out.println("Enter password:");
                String password = command_reader.readLine();
                Credentials cred = new Credentials(username, password);
                Request login = new Request(cred.loginString());

                out.writeObject(login);
                out.flush();

                System.out.println("here2");
                Reply reply = (Reply) in.readObject();
                if (reply.getStatusCode().equals("OK")) {
                    System.out.println("Login successful");
                    break;
                } else {
                    System.out.println("Login failed");

                }


            }

            System.out.println("Enter command: (help for more info)");
            while (true) {
                try {
                    command = command_reader.readLine();
                    if (handle_command(command, command_reader))
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