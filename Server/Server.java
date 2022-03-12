import java.io.IOException;

import auth.Auth;

public class Server {
    private static Auth auth;

    public static void main(String[] args) {
        try {
            auth = new Auth("Server/runfiles/users");
        } catch(IOException io) {
            io.printStackTrace();
            return;
        }
    }
}