package com.Client;

import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

import com.Client.config.ConfigClient;
import com.Client.conn.ClientConnection;
import com.DataTransfer.FileTransfer;
import com.DataTransfer.Reply;
import com.DataTransfer.Request;
import com.DataTransfer.chunks.FileChunk;
import com.DataTransfer.threads.ClientFileDownload;
import com.DataTransfer.threads.ClientUpload;
import com.Server.conn.ServerConnection;
import com.User.Credentials;
import com.enums.ResponseStatus;

public class CommandHandler {

    private Reply reply;
    private ClientConnection clientConnection;
    private Socket socket;
    private InetAddress addInUse;
    private int portInUse;
    ConfigClient config;

    public CommandHandler() {
        this.clientConnection = null;
        this.socket = null;
    }

    public int getPortInUse() {
        return portInUse;
    }

    public void setPortInUse(int portInUse) {
        this.portInUse = portInUse;
    }

    public InetAddress getAddInUse() {
        return addInUse;
    }

    public void setAddInUse(InetAddress addInUse) {
        this.addInUse = addInUse;
    }

    public CommandHandler(ClientConnection clientConnection, Socket socket, InetAddress address, int port,
            ConfigClient config) {
        this.clientConnection = clientConnection;
        this.socket = socket;
        this.config = config;
        this.setPortInUse(port);
        this.setAddInUse(address);

    }

    public void login(Scanner commandReader) throws SocketTimeoutException, SocketException {

        while (true) {
            try {
                System.out.println("Enter username:");
                String username = commandReader.nextLine();
                System.out.println("Enter password:");
                Console c = System.console();
                String password = new String(c.readPassword());// commandReader.readLine();
                Credentials cred = new Credentials(username, password);
                Request login = new Request(cred.loginString(), "");

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
            } catch (SocketTimeoutException | SocketException e) {
                throw e;
            } catch (IOException e) {
                System.out.println("Error while sending or reading login information");
                e.printStackTrace();
            }

        }
    }

    public void changePassword(Scanner commandReader) throws SocketTimeoutException, SocketException {
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
            if (reply.getStatusCode().equals(ResponseStatus.OK.getStatus())) {
                System.out.println("Password change successful! You have been logged out");
                break;
            } else {
                System.out.println("Password change failed. Trying again...");
            }

        }
        Client.setToken("");

        login(commandReader);
    }

    public void serverLs(String line) throws SocketTimeoutException, SocketException {
        String[] ls = line.split(" ", 2); // ! check if double spaces
        String token = Client.getToken();
        Request req;
        String dir = "";

        if (ls.length > 1) {

            dir = ls[1];
            String serverDir = Client.getServerDir();

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

            req = new Request("LS\n" + dir, token); // idempotent
        } else {
            req = new Request("LS", token); // idempotent
        }

        while (true) {

            clientConnection.sendRequest(req);
            Reply reply = clientConnection.getReply();
            if (reply.getStatusCode().equals(ResponseStatus.OK.getStatus())) {
                System.out.println(reply.getMessage());
                break;
            } else {
                System.out.println("Ls failed: " + reply.getMessage());
                break;
            }

        }
    }

    public void changeServerWorkingDirectory(String command) throws SocketTimeoutException, SocketException {

        String dir = "";
        String[] sp = command.split(" ", 2);
        // String dir = command.split(" ", 2)[1]; // ! check if double spaces
        if (sp.length >= 2)
            dir = sp[1];
        String serverDir = Client.getServerDir();
        // System.out.println("Server dir: " + serverDir);
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
            if (reply.getStatusCode().equals(ResponseStatus.OK.getStatus())) {
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
        } catch (NoSuchFileException nsf) {
            System.out.println("Directory not found: " + nsf.getMessage());
        } catch (IOException io) {
            System.out.println("Error: " + io.getMessage());
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
                clientDir = clientDir.substring(0, lastSlash);
            } else {
                clientDir = clientDir + "/" + p;
            }
        }
        Path p = Paths.get(clientDir);
        if (Files.exists(p) && Files.isDirectory(p)) {
            Client.setClientDir(clientDir);
            System.out.println("new directory: " + clientDir);
        } else {
            System.out.println("Error: directory not found");
        }
    }

    public void downloadFile(String command) throws SocketTimeoutException, SocketException {
        String[] sp = command.split(" ", 2);
        if (sp.length < 2) {
            System.out.println("Command format : <download> <file name>");
            return;
        } else {

            String dir = sp[1];
            String serverDir = Client.getServerDir();

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
        }
        Request req = new Request("DOWNLOAD\n" + sp[1], Client.getToken());
        clientConnection.sendRequest(req);
        Reply rep = clientConnection.getReply();
        if (!rep.getStatusCode().equals(ResponseStatus.OK.getStatus()) || !rep.getMessage().equals("FILE EXISTS")) {
            System.out.println("Problems acquiring the specified file: " + rep.getMessage());
            return;
        }
        try {
            // ! must be closed somehow
            ServerSocket serverSocket = new ServerSocket(0);
            // port dynamically allocated
            // !check if socket is valid
            System.out.println("Acquired port: " + serverSocket.getLocalPort());
            req = new Request("PORT\n" + serverSocket.getLocalPort(), Client.getToken());
            clientConnection.sendRequest(req);

            new ClientFileDownload(serverSocket, Client.getClientDir());

        } catch (SocketTimeoutException | SocketException e) {
            throw e;
        } catch (IOException io) {
            System.out.println("Problems trying to download: " + io.getMessage());
            io.printStackTrace();
        }
    }

    public void uploadFile(String command) throws SocketTimeoutException, SocketException {
        String[] sp = command.split(" ", 2);
        if (sp.length < 2) {
            System.out.println("Invalid command. The structure is: upload <file_name>");
        }
        String fileName = sp[1];
        try {
            Path filePath = Paths.get(Client.getClientDir(), fileName);
            if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
                System.out.println("File " + fileName + " not found");
                return;
            }
            clientConnection.constructAndSendRequest("UPLOAD", Client.getToken());
            Reply rep = clientConnection.getReply();
            new ClientUpload(fileName, rep.getMessage(), socket.getInetAddress());
        } catch (Exception e) {
            System.out.println("Problems uploading file: " + e.getMessage());
        }
    }

    public Socket getSocket() {
        return socket;
    }

    private boolean tryToReconnect(String ip, int port) {
        try {

            this.socket = new Socket(ip, port);
            this.socket.setSoTimeout(Client.CLIENT_SOCKET_TIMEOUT_MILLISECONDS);

            ObjectOutputStream oos = new ObjectOutputStream(this.socket.getOutputStream());
            oos.flush();
            ObjectInputStream ois = new ObjectInputStream(this.socket.getInputStream());
            this.clientConnection.setSocketParams(this.socket, ois, oos);
            return true;
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return false;
        }
    }

    public boolean changeServerInfo(Scanner commandReader) {

        try {
            Scanner sc = new Scanner(System.in);
            Request abort = new Request("ABORT", Client.getToken());

            clientConnection.sendRequest(abort);

            this.socket.close();
            System.out.println("Client closing connection");

            System.out.println("Enter new primary server ip: ");
            config.setPrimaryServerName(sc.next());
            System.out.println("Enter new primary server port: ");
            config.setPrimaryServerPort(sc.nextInt());
            System.out.println("Enter new secondary server ip: ");
            config.setSecondaryServerName(sc.next());
            System.out.println("Enter new secondary server port: ");
            config.setSecondaryServerPort(sc.nextInt());

            config.saveInfo();

            // tryToReconnect();
            // ! check if primary is down
            if (!tryToReconnect(config.getPrimaryServerName(), config.getPrimaryServerPort())) {
                if (!tryToReconnect(config.getSecondaryServerName(), config.getSecondaryServerPort())) {
                    System.out.println("Servers are down or your information is incorrect");
                    System.out.println("Could not connect. Shutting down client application");
                    return true; // true means exit
                }
            }

            System.out.println("Enter your credentials again");
            login(commandReader);

            return false; // false means the app continues

        } catch (IOException e) {
            e.printStackTrace();
        }
        return true; // truen means exit

    }

    public boolean handleCommand(String line, Scanner commandReader)
            throws SocketTimeoutException, SocketException {
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
                System.out.println("\tserver-ls / sls -> Show files in the current server directory");
                System.out.println("\tserver-cd / scd dir_name -> Change server directory to dir_name");
                System.out.println("\tclient-ls / cls -> Show files in the current client directory");
                System.out.println("\tclient-cd / ccd dir_name -> Change client directory to dir_name");
                System.out.println("\tdownload file_name -> Download file_name from server");
                System.out.println("\tupload file_name -> Upload file_name to server");
                System.out.println("\texit -> Exit client application");
                System.out.println("\thelp -> Show command list");
                break;
            case "ch-pass":
                changePassword(commandReader);
                break;
            case "ch-server-info":
                return changeServerInfo(commandReader);
            case "server-ls":
            case "sls":
                serverLs(line);
                break;
            case "server-cd":
            case "scd":
                changeServerWorkingDirectory(line);
                break;
            case "client-ls":
            case "cls":
                clientLs(line);
                break;
            case "client-cd":
            case "ccd":
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