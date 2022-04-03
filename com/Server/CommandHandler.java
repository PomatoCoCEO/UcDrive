package com.Server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.DataTransfer.Reply;
import com.DataTransfer.Request;
import com.DataTransfer.tasks.FileDownloadTask;
import com.DataTransfer.tasks.FileUploadTask;
import com.DataTransfer.threads.ServerDownload;
import com.DataTransfer.threads.UDPTransfer;
import com.Server.auth.User;
import com.Server.auth.Auth.Operation;
import com.Server.config.ConfigServer;
import com.Server.conn.ServerConnection;
import com.Server.except.AuthorizationException;
import com.Server.udp.UDPCommandSender;
import com.enums.ResponseStatus;
import com.DataTransfer.FileTransfer;

public class CommandHandler {
    Socket socket;
    Reply reply;
    ServerConnection serverConnection;

    public CommandHandler(Socket socket, ServerConnection serverConnection) {
        this.socket = socket;
        this.serverConnection = serverConnection;
    }

    public void verifyRequest(Request request) throws AuthorizationException, SocketException{
        String comm = request.getMessage().split("\n")[0];
        if (!comm.equals("LOGIN")) {
            if (!request.getToken().equals(serverConnection.getUser().getToken())) {
                serverConnection.sendReply(new Reply("Wrong authentication", "Unauthorized"));
                throw new AuthorizationException(
                        "Invalid auth token for user " + serverConnection.getUser().getUsername());
            }
        }
    }

    public void handleRequest(Request request) throws AuthorizationException, SocketException {
        verifyRequest(request);
        String[] sp = request.getMessage().split("\n");
        switch (sp[0]) {
            case "CH-PASS":
                changePassword(request);
                break;
            case "LOGIN":
                login(request);
                break;
            case "LS":
                handleLs(request);
                break;
            case "CD":
                handleCd(request);
                break;
            case "DOWNLOAD":
                handleDownload(request);
                break;
            case "UPLOAD":
                handleUpload();
                break;
            case "ABORT":
                this.serverConnection.getUser().setToken("");
                this.serverConnection.close();
                break;
            // case EXIT: close socket and all
        }

    }

    public void handleDownload(Request request) {
        String[] sp = request.getMessage().split("\n");
        if (sp.length < 2) {
            serverConnection.constructAndSendReply("Not enough arguments", "Bad Request");
            return;
        }

        String fileName = sp[1];
        Path absolutePath = Paths.get(serverConnection.getAbsolutePath(), serverConnection.getUser().getServerDir(),
                fileName);
        Path filePath = Paths.get(serverConnection.getUser().getServerDir(),
                fileName);
        String absolutePathString = absolutePath.toString();
        File f = new File(absolutePathString);
        if (!f.exists() || !f.isFile()) {
            serverConnection.constructAndSendReply("Inexistent file", "Bad Request");
            return;
        }
        serverConnection.constructAndSendReply("FILE EXISTS", ResponseStatus.OK.getStatus());
        // ? this section of code needs to remain here because the request should be
        // handled in a single thread
        System.out.println("File exists sent");
        Request portNoReq = serverConnection.getRequest();
        System.out.println("Got answer port no" + portNoReq);
        String[] msgPort = portNoReq.getMessage().split("\n");
        if (msgPort.length < 2 || !msgPort[0].equals("PORT")) {
            serverConnection.constructAndSendReply("Insufficient port information", "Bad Request");
            return;
        }
        int portNo = Integer.parseInt(msgPort[1]);
        System.out.println("Got port: " + portNo);

        FileDownloadTask ftt = new FileDownloadTask(filePath.toString(), socket.getInetAddress(), portNo,
                serverConnection);

        serverConnection.getServer().getQueueFileSend().add(ftt);

        // new ServerDownload(socket.getInetAddress(), portNo, absolutePath, fileName,
        // serverConnection);

        // new FileTransfer(oisSocket, oosSocket, bytes, noBlocks, dirPath, fileName,
        // true);

    }

    public void handleUpload() {
        try {
            System.out.println("Handling upload"); // handles upload only once
            ServerSocket listenUploadSocket = new ServerSocket(0);
            System.out.println("New port opened for file transfer: " + listenUploadSocket.getLocalPort());
            serverConnection.constructAndSendReply("PORT " + listenUploadSocket.getLocalPort(),
                    ResponseStatus.OK.getStatus());
            FileUploadTask fut = new FileUploadTask(serverConnection, listenUploadSocket);
            serverConnection.getServer().getQueueFileRcv().add(fut);
            System.out.println("Added to quuuuuuuuuue");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void login(Request request) throws SocketException{
        Reply reply;
        boolean authenticated = false;
        boolean first = true;
        while (!authenticated) {
            try {
                if (!first)
                    request = serverConnection.getRequest();
                first = false;
                /*
                 * this is a simple way to ensure the message is read correctly.
                 * \n's cannot be read in the console
                 */
                String[] sp = request.getMessage().split("\n");
                if (!sp[0].equals("LOGIN")) {
                    throw new Exception("Invalid command");
                }
                serverConnection.setUser(serverConnection.getAuth().authenticate(sp[1], sp[2]));
                authenticated = true;
                // ! we need to determine a token!
                reply = new Reply(
                        sp[1] + "\n" + serverConnection.getUser().getServerDir() + "\n"
                                + serverConnection.getUser().getClientDir(),
                        ResponseStatus.OK.getStatus());
                // sends a token (to be implemented) and the last working directory
                serverConnection.sendReply(reply);
            } catch(SocketException se) {
                throw se;
            } 
            catch (Exception e) {
                System.err.println("No authentication was possible");
                reply = new Reply("Login unsuccessful", "Unauthorized");
                serverConnection.sendReply(reply);
            }
            // determine a suitable token
        }
    }

    private void changePassword(Request request) throws SocketException {
        String[] sp = request.getMessage().split("\n");
        System.out.println("Split");
        String newPassword = sp[1];
        User user = serverConnection.getUser();
        User userChanged = new User(user.getUsername(), newPassword, user.getServerDir(), user.getClientDir());
        serverConnection.getAuth().changeUsers(Operation.CHANGE, userChanged);
        System.out.println("Changed");
        Reply reply = new Reply("Password changed!", ResponseStatus.OK.getStatus());
        serverConnection.sendReply(reply);
        String failOverChangePassword = "CH-PASS\n" + serverConnection.getUser().getUsername() + "\n" + newPassword;
        serverConnection.getServer().getThreadPoolUdpCommandSend()
                .execute(new UDPCommandSender(serverConnection.getServer(), failOverChangePassword));
    }

    private void handleLs(Request request) throws SocketException {
        String[] sp = request.getMessage().split("\n", 2);
        String relativePath = "";
        // if it has no argument then we just get the user's last directory
        System.out.println("Relative path: " + relativePath);
        if (sp.length >= 2) {
            // else we need to add a relative part to the path
            relativePath = sp[1];
        }
        String currentPath = serverConnection.getUser().getServerDir();
        System.out.println("Current path : " + currentPath);
        // ! we could do some verification here
        Path p = Paths.get(serverConnection.getAbsolutePath(), currentPath, relativePath);
        // ! we shall not share the internal structure of the server with the clients!!!

        Reply reply = new Reply("Invalid path", "Internal Server Error"); // will not be changed if there is an error
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
            reply = new Reply(sb.toString(), ResponseStatus.OK.getStatus());
        } catch (IOException io) {
            io.printStackTrace();
        } finally {

            serverConnection.sendReply(reply);
        }
    }

    private void handleCd(Request request) {
        String[] sp = request.getMessage().split("\n", 2);
        String relativePath = "";
        // if it has no argument then we just get the user's last directory
        if (sp.length >= 2) {
            // else we need to add a relative part to the path
            relativePath = sp[1];
        } else {
            // go to the default directory
            relativePath = "";
        }
        try (DirectoryStream<Path> dStream = Files
                .newDirectoryStream(Paths.get(serverConnection.getAbsolutePath(), relativePath))) {
            // just to make sure that this directory actually exists
            serverConnection.getUser().setServerDir(relativePath);
            serverConnection.getAuth().changeUsers(Operation.CHANGE, serverConnection.getUser());
            serverConnection.constructAndSendReply("Directory changed", ResponseStatus.OK.getStatus());

            User user = serverConnection.getUser();
            try {
                String pathWrite = Paths.get(serverConnection.getServer().getConfigPath(), "usr", user.getUsername())
                        .toString();
                BufferedWriter writer = new BufferedWriter(
                        new FileWriter(pathWrite));
                writer.write(user.toFileString());

                writer.close();

            } catch (IOException e) {
                e.printStackTrace();
            }

            String failOverCd = "SCD\n" + serverConnection.getUser().getUsername() + "\n" + relativePath;
            serverConnection.getServer().getThreadPoolUdpCommandSend()
                    .execute(new UDPCommandSender(serverConnection.getServer(), failOverCd));
            // ! send to secondary

        } catch (IOException io) {
            serverConnection.constructAndSendReply("Invalid directory", "Bad Request");
            io.printStackTrace();
        }
        System.out.println("New path for user: " + serverConnection.getUser().getServerDir());
    }
}