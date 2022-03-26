package com.Server;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.DataTransfer.Reply;
import com.DataTransfer.Request;
import com.Server.auth.User;
import com.Server.auth.Auth.Operation;
import com.Server.conn.ServerConnection;
import com.Server.except.AuthorizationException;
import com.DataTransfer.FileTransfer;

public class CommandHandler {
    Socket socket;
    Reply reply;
    ServerConnection serverConnection;

    public CommandHandler(Socket socket, ServerConnection serverConnection) {
        this.socket = socket;
        this.serverConnection = serverConnection;
    }

    public void verifyRequest(Request request) throws AuthorizationException {
        String comm = request.getMessage().split("\n")[0];
        if (!comm.equals("LOGIN")) {
            if (!request.getToken().equals(serverConnection.getUser().getToken())) {
                serverConnection.sendReply(new Reply("Wrong authentication", "Unauthorized"));
                throw new AuthorizationException(
                        "Invalid auth token for user " + serverConnection.getUser().getUsername());
            }
        }
    }

    public void handleRequest(Request request) throws AuthorizationException {
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
                handleUpload(request);
                break;
        }

    }

    public void handleDownload(Request request) {
        String [] sp = request.getMessage().split("\n");
        if(sp.length<2) {
            serverConnection.constructAndSendReply("Not enough arguments", "Bad Request");
            return;
        }
        try {
            String fileName = sp[1];
            Path absolutePath = Paths.get(serverConnection.getAbsolutePath(),serverConnection.getUser().getServerDir(), fileName);
            String absolutePathString = absolutePath.toString();
            File f = new File(absolutePathString);
            if(!f.exists() || !f.isFile()) {
                serverConnection.constructAndSendReply("Inexistent file", "Bad Request");
                return;
            }
            serverConnection.constructAndSendReply("FILE EXISTS", "OK");
            System.out.println("File exists sent");
            Request portNoReq = serverConnection.getRequest();
            System.out.println("Got answer port no"+portNoReq);
            String[] msgPort = portNoReq.getMessage().split("\n");
            if(msgPort.length<2||!msgPort[0].equals("PORT")) {
                serverConnection.constructAndSendReply("Insufficient port information", "Bad Request");
                return;
            }
            int portNo = Integer.parseInt(msgPort[1]);
            System.out.println("Got port: "+portNo);
            Socket sendSocket = new Socket(socket.getInetAddress(), portNo);
            ObjectInputStream oisSocket = new ObjectInputStream(sendSocket.getInputStream());
            ObjectOutputStream oosSocket = new ObjectOutputStream(sendSocket.getOutputStream());
            oosSocket.flush();
            long bytes = Files.size(absolutePath);
            long noBlocks = bytes / (FileTransfer.BLOCK_BYTE_SIZE) + (bytes % (FileTransfer.BLOCK_BYTE_SIZE)==0?0:1);
            System.out.println();
            String dirPath = Paths.get(serverConnection.getAbsolutePath(),serverConnection.getUser().getServerDir()).toString();
            new FileTransfer(oisSocket, oosSocket, bytes, noBlocks,dirPath, fileName,true);
            
        } catch(IOException io) {
            io.printStackTrace();
            serverConnection.constructAndSendReply("Server Error: "+io.getMessage(), "Internal Server Error");
        }
    }

    public void handleUpload(Request request) {
        try(ServerSocket listenUploadSocket = new ServerSocket(0)) {
            serverConnection.constructAndSendReply("PORT "+listenUploadSocket.getLocalPort(), "OK");
            Socket uploadSocket = listenUploadSocket.accept();
            ObjectOutputStream oos = new ObjectOutputStream(uploadSocket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(uploadSocket.getInputStream());
            Reply rep = (Reply) ois.readObject();
            String fileMetaData = rep.getMessage();
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
            String dirPath = Paths.get(serverConnection.getAbsolutePath(),serverConnection.getUser().getServerDir()).toString();
            new FileTransfer(ois, oos, byteSize, blockNumber, dirPath, name,false);
        } catch(IOException io ) {
            io.printStackTrace();
        } catch(ClassNotFoundException cnf) {
            cnf.printStackTrace();
        }
        
    }

    public void login(Request request) {
        String response;
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
                        "OK");
                // sends a token (to be implemented) and the last working directory
                serverConnection.sendReply(reply);
            } catch (Exception e) {
                System.err.println("No authentication was possible");
                reply = new Reply("Login unsuccessful", "Unauthorized");
                serverConnection.sendReply(reply);
            }
            // determine a suitable token
        }
    }

    private void changePassword(Request request) {
        String[] sp = request.getMessage().split("\n");
        System.out.println("Split");
        String newPassword = sp[1];
        User user = serverConnection.getUser();
        User userChanged = new User(user.getUsername(), newPassword, user.getServerDir(), user.getClientDir());
        serverConnection.getAuth().changeUsers(Operation.CHANGE, userChanged);
        System.out.println("Changed");
        Reply reply = new Reply("Password changed!", "OK");
        serverConnection.sendReply(reply);
    }

    private void handleLs(Request request) {
        String[] sp = request.getMessage().split("\n", 2);
        String relativePath = "";
        // if it has no argument then we just get the user's last directory
        System.out.println("Relative path: "+relativePath);
        if (sp.length >= 2) {
            // else we need to add a relative part to the path
            relativePath = sp[1];
        }
        String currentPath = serverConnection.getUser().getServerDir();
        System.out.println("Current path : "+currentPath);
        // ! we could do some verification here
        Path p = Paths.get(serverConnection.getAbsolutePath(),currentPath, relativePath); 
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
            reply = new Reply(sb.toString(), "OK");
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
            relativePath =  sp[1];
        } else {
            // go to the default directory
            relativePath = "";
        }
        try (DirectoryStream<Path> dStream = Files.newDirectoryStream(Paths.get(serverConnection.getAbsolutePath(), relativePath))) {
            // just to make sure that this directory actually exists
            serverConnection.getUser().setServerDir(relativePath);
            serverConnection.getAuth().changeUsers(Operation.CHANGE, serverConnection.getUser());
            serverConnection.constructAndSendReply("Directory changed", "OK");
        } catch (IOException io) {
            serverConnection.constructAndSendReply("Invalid directory", "Bad Request");
            io.printStackTrace();
        }
        System.out.println("New path for user: " + serverConnection.getUser().getServerDir());
    }
}