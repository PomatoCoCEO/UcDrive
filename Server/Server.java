import java.io.IOException;
import java.net.ServerSocket;
import auth.Auth;
import conn.ServerConnection;
import config.ConfigServer;

public class Server {
    private static Auth auth;
    private static ServerSocket serverSocket;

    public static void main(String[] args) {
        try {
            auth = new Auth("Server/runfiles/users");
            ConfigServer config = new ConfigServer("Server/runfiles/config");
            ServerSocket listenSocket = new ServerSocket(config.getSocketPort());
            System.out.println("LISTEN SOCKET="+listenSocket);
            while(true) {
                Socket clientSocket = listenSocket.accept(); // BLOQUEANTE
                System.out.println("CLIENT_SOCKET (created at accept())=" + clientSocket);
                new ServerConnection(clientSocket).start();
            }
            
        } catch(IOException io) {
            io.printStackTrace();
            return;
        }
    }
}
