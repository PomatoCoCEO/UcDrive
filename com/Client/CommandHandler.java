package com.Client;

import java.io.BufferedReader;
import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.Client.conn.ClientConnection;
import com.DataTransfer.FileChunk;
import com.DataTransfer.FileTransfer;
import com.DataTransfer.Reply;
import com.DataTransfer.Request;
import com.Server.conn.ServerConnection;
import com.User.Credentials;

public class CommandHandler {

    Reply reply;
    ClientConnection clientConnection;
    Socket socket;

    public CommandHandler(ClientConnection clientConnection, Socket socket) {
        this.clientConnection = clientConnection;
        this.socket = socket;
    }

    public void login(BufferedReader commandReader) {

        while (true) {
            try {
                System.out.println("Enter username:");
                String username = commandReader.readLine();
                System.out.println("Enter password:");
                Console c = System.console();
                String password = new String(c.readPassword());// commandReader.readLine();
                Credentials cred = new Credentials(username, password);
                Request login = new Request(cred.loginString());

                clientConnection.sendRequest(login);

                Reply reply = clientConnection.getReply();
                switch (reply.getStatusCode()) {
                    case "OK":
                        System.out.println("Login successful " + reply.getMessage());
                        String server_data = reply.getMessage();
                        String[] data = server_data.split("\n");
                        Client.setToken(data[0]);
                        Client.setServerDir(data[1]); // this is the problematic part
                        Client.setClientDir(System.getProperty("user.dir") + "/com/Client/Data");
                        return;
                    case "Unauthorized":
                        System.out.println("Wrong username or password");
                        break;
                    default:
                        System.out.println("Login failed");
                }
            } catch (IOException e) {
                System.out.println("Error while sending or reading login information");
                e.printStackTrace();
            }

        }
    }

    public void changePassword(BufferedReader commandReader) {
        Console cons = System.console();
        boolean first = true;
        String confirmPass, newPass;
        do {
            if (!first)
                System.out.println("Passwords do not match! Try again");
            System.out.println("Enter new password:");
            newPass = new String(cons.readPassword());
            System.out.println("Confirm password:");
            confirmPass = new String(cons.readPassword());
            first = false;
        } while (!newPass.equals(confirmPass));

        Request req = new Request("CH-PASS\n" + newPass, Client.getToken());
        while (true) {

            clientConnection.sendRequest(req);
            Reply reply = clientConnection.getReply();
            if (reply.getStatusCode().equals("OK")) {
                System.out.println("Password change successful! You have been logged out");
                break;
            } else {
                System.out.println("Password change failed. Trying again...");
            }

        }
        Client.setToken("");

        login(commandReader);
    }

    public void serverLs(String line) {
        String[] ls = line.split(" ", 2); // ! check if double spaces
        String token = Client.getToken();
        Request req;
        if (ls.length > 1) {
            req = new Request("LS\n" + ls[1], token); // idempotent
        } else {
            req = new Request("LS", token); // idempotent
        }

        while (true) {

            clientConnection.sendRequest(req);
            Reply reply = clientConnection.getReply();
            if (reply.getStatusCode().equals("OK")) {
                System.out.println(reply.getMessage());
                break;
            } else {
                System.out.println("Ls failed: " + reply.getMessage());
                break;
            }

        }
    }

    public void changeServerWorkingDirectory(String command) {

        String dir = "";
        String[] sp = command.split(" ", 2);
        // String dir = command.split(" ", 2)[1]; // ! check if double spaces
        if (sp.length >= 2)
            dir = sp[1];
        String serverDir = Client.getServerDir();
        System.out.println("Server dir: " + serverDir);
        String[] paths = dir.split("/");

        for (String p : paths) {
            if (p.equals("..")) {
                int lastSlash = serverDir.lastIndexOf("/");
                if (lastSlash == -1) {
                    System.out.println("Invalid dir");
                    return;
                }
                serverDir = serverDir.substring(0, lastSlash);

            } else {
                serverDir = serverDir + "/" + p;
            }
        }
        // client server-cd should not be handled before response but after a successful
        // response

        Request req = new Request("CD\n" + serverDir, Client.getToken());
        while (true) {

            clientConnection.sendRequest(req);
            Reply reply = clientConnection.getReply();
            if (reply.getStatusCode().equals("OK")) {
                System.out.println(reply.getMessage());
                Client.setServerDir(serverDir);
                break;
            } else if (reply.getStatusCode().equals("Bad Request")) {
                System.out.println("Invalid command: " + reply.getMessage());
                break;
            } else {
                System.out.println("Cd failed. Trying again...");
            }

        }
    }

    public void clientLs(String command) {

        String[] sp = command.split(" ", 2);
        String relativePath = "";
        if (sp.length >= 2) {
            relativePath = sp[1];
        }

        Path p = Paths.get(Client.getClientDir() + "/" + relativePath);
        try (DirectoryStream<Path> dStream = Files.newDirectoryStream(p)) {
            // String[] ans = dStream.
            StringBuilder sb = new StringBuilder("");
            for (Path filePath : dStream) {
                String aid = filePath.toString();
                // just to make this platform-independent
                if (aid.indexOf("/") >= 0)
                    sb.append(aid.substring(aid.lastIndexOf("/") + 1));
                else if (aid.indexOf("\\") >= 0) {
                    sb.append(aid.substring(aid.lastIndexOf("\\") + 1));
                } else
                    sb.append(aid);
                sb.append('\n');
            }
            System.out.println(sb.toString());
        } catch(NoSuchFileException nsf) {
            System.out.println("Directory not found: "+nsf.getMessage());
        } 
        catch (IOException io) {
            System.out.println("Error: "+io.getMessage());
        } 

    }

    public void clientCd(String command) {
        String[] cd = command.split(" ", 2);
        String dir = "";

        String clientDir = Client.getClientDir();

        if (cd.length > 1) {
            dir = cd[1].trim();
        } else {
            clientDir = System.getProperty("user.dir") + "/com/Client/Data";
            // System.out.println("Please specify the relative path of the new directory.");
            return;
        }

        String[] paths = dir.split("[/\\\\]"); // you can have both cases

        for (String p : paths) {

            if (p.equals("..")) {
                int lastSlash = clientDir.lastIndexOf("/"); // linux notation
                if (lastSlash == -1)
                    lastSlash = clientDir.lastIndexOf("\\"); // ms dos notation
                if (lastSlash == -1) {
                    System.out.println("Invalid dir");
                    return;
                }
                clientDir=clientDir.substring(0, lastSlash);
            } else {
                clientDir = clientDir + "/" + p;
            }
        }
        Path p = Paths.get(clientDir);
        if(Files.exists(p) && Files.isDirectory(p)) {
            Client.setClientDir(clientDir);
            System.out.println("new directory: " + clientDir);
        }
        else {
            System.out.println("Error: directory not found");
        }
    }

    public void downloadFile(String command) {
        String[] sp = command.split(" ", 2);
        if (sp.length < 2) {
            System.out.println("Command format : <download> <file name>");
            return;
        }
        Request req = new Request("DOWNLOAD\n" + sp[1], Client.getToken());
        clientConnection.sendRequest(req);
        Reply rep = clientConnection.getReply();
        if (!rep.getStatusCode().equals("OK") || !rep.getMessage().equals("FILE EXISTS")) {
            System.out.println("Problems acquiring the specified file: " + rep.getMessage());
            return;
        }

        try (ServerSocket serverSocket = new ServerSocket(0)) {
            // port dynamically allocated
            // !check if socket is valid
            System.out.println("Acquired port: "+serverSocket.getLocalPort());
            req = new Request("PORT\n" + serverSocket.getLocalPort(), Client.getToken());
            clientConnection.sendRequest(req);
            Socket receiver = serverSocket.accept();
            ObjectOutputStream oos = new ObjectOutputStream(receiver.getOutputStream());
            oos.flush();
            ObjectInputStream ois = new ObjectInputStream(receiver.getInputStream());
            reply = (Reply) ois.readObject();
            String fileMetaData = reply.getMessage();
            String[] fileDataSplit = fileMetaData.split("\n");
            if (fileDataSplit.length < 6 ||
                    !fileDataSplit[0].equals("FILE") ||
                    !fileDataSplit[2].equals("SIZE") ||
                    !fileDataSplit[4].equals("BLOCKS")) {
                System.err.println("Errors communicating with the server");
                return;
            }
            String name = fileDataSplit[1];
            long byteSize = Integer.parseInt(fileDataSplit[3]);
            long blockNumber = Integer.parseInt(fileDataSplit[5]);
            System.out.printf("Name: %s, byteSize: %d, BlockNumber: %s\n", name, byteSize, blockNumber);

            new FileTransfer(ois, oos, byteSize, blockNumber, Client.getClientDir(), name,false);
            // ft.join(); // do we wait for the conclusion of the transfer?
                       // ! dont think we do
        } catch (IOException io) {
            System.out.println("Problems trying to download: " + io.getMessage());
            io.printStackTrace();
        } catch (ClassNotFoundException cnf) {
            System.out.println("Problems trying to download: " + cnf.getMessage());
            cnf.printStackTrace();
        }
    }

    public void uploadFile(String command) {
        String[] sp = command.split(" ",2);
        if(sp.length<2) {
            System.out.println("Invalid command. The structure is: upload <file_name>");
        }
        String fileName = sp[1];
        try {
            Path filePath = Paths.get(Client.getClientDir(), fileName);
            if(!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
                System.out.println("File "+fileName+" not found");
                return;
            }
            clientConnection.constructAndSendRequest("UPLOAD", Client.getToken());
            Reply rep = clientConnection.getReply();
            String[] portInfoSp = rep.getMessage().split(" ");
            if(!portInfoSp[0].equals("PORT")) {
                System.out.println("Problems uploading file: "+rep.getMessage());
                return;
            }
            int portNo = Integer.parseInt(portInfoSp[1]);
            Socket uploadSocket = new Socket(socket.getInetAddress(), portNo);
            ObjectInputStream oisSocket = new ObjectInputStream(uploadSocket.getInputStream());
            ObjectOutputStream oosSocket = new ObjectOutputStream(uploadSocket.getOutputStream());
            oosSocket.flush();
            long bytes = Files.size(filePath);
            long noBlocks = bytes / (FileTransfer.BLOCK_BYTE_SIZE) + (bytes % (FileTransfer.BLOCK_BYTE_SIZE)==0?0:1);
            System.out.println();
            new FileTransfer(oisSocket, oosSocket, bytes, noBlocks,Client.getClientDir(), fileName,true);
        } catch(IOException io) {
            System.out.println("Problems uploading file: "+io.getMessage());
            io.printStackTrace();
        } 
        catch(Exception e) {
            System.out.println("Problems uploading file: "+e.getMessage());
        }
    }

    public boolean handleCommand(String line, BufferedReader commandReader) {
        String[] commands = line.split(" ", 2);
        String command = commands[0];
        switch (command) {
            case "exit":
                return true;
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
                changePassword(commandReader);
                break;
            case "server-ls":
                serverLs(line);
                break;
            case "server-cd":
                changeServerWorkingDirectory(line);
                break;
            case "client-ls":
                clientLs(line);
                break;
            case "client-cd":
                clientCd(line);
                break;
            case "download":
                downloadFile(line);
                break;
            case "upload":
                uploadFile(line);
                break;
            default:
                System.out.println("Not a valid command");
        }

        return false;
    }
}