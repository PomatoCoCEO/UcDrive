package conn;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import server.auth.Auth;

public class ServerConnection implements Runnable {
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private Auth auth;
    public ServerConnection(Socket socket, Auth auth) {
        this.socket = socket;
        this.auth = auth;
        in = new ObjectInputStream(socket.getInputStream());
        out = new ObjectOutputStream(socket.getOutputStream());
        this.start();
    }
    
    public void run() {
        String response;
        Request request;
        Reply reply;
        boolean authenticated = false;
        while (!authenticated) {
            request = (Request) in.readObject();
            String[] sp = request.getMessage().split(" ");
            try{
                auth.authenticate(sp[2], sp[4]);
                authenticated = true;
                // determine suitable token
                reply = new Reply(sp[2], "OK");
            }
            catch(Exception e) {
                System.err.println("No authentication was possible");
                request.setMessage("Login unsuccessful");
                reply = new Reply("", "Unauthorized");
                // 
            }
            finally {
                out.writeObject(reply);
            }
            // determine a suitable token
        }
        System.out.println("Authentication successful! Let us continue!");
        while(true) {
            request = in.readUTF();
            System.out.println("Request: \""+request+"\"");
            // handle request

            // and send response
            

        }
    }
}
