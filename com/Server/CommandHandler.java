package com.Server;
public class CommandHandler {
    Socket socket;
    Reply reply;
    ServerConnection serverConnection;
    public CommandHandler(Socket socket, ServerConnection serverConnection) {
        this.socket = socket;
        this.serverConnection = serverConnection;
    }

    public void handleRequest(Request request) {
        String[] sp = request.getMessage().split("\n");
        switch(sp[0]) {
            case "ch-pass":
                

        }


    }
}