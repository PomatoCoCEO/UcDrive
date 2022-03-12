package auth;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.TreeSet;
import java.util.stream.Stream;

public class Auth {
    TreeSet<User> users;
    public void loadUsers(String fileName) throws IOException {
        File fr = new File(fileName);
        try (Scanner sc = new Scanner(fr)) {
            // BufferedReader br = new BufferedReader(fr);
            while(sc.hasNextLine()) {
                String line = sc.nextLine();
                String [] sp = line.split(" ");
                User u = new User(sp[0], sp[1], sp[2]);
                users.add(u);
            }
        } catch(FileNotFoundException f) {
            System.out.println("File not found: "+f.getMessage());
            f.printStackTrace();
        }
    }

    public void registerUser(String username, String password) {
        //! falta obter a hash da password
        User user = new User(username, password, "");
        users.add(user);
    }

    public Auth(String fileName) throws IOException{
        users = new TreeSet<>();
        loadUsers(fileName);
    }

    public String authenticate(String username, String password) throws Exception {
        User found = users.ceiling(new User(username, "", ""));
        if(!found.getUsername().equals(username) || !found.getPasswordHash().equals(password)) {
            throw new Exception("Authentication failed: user "+found);
        }
        return "Yay";
    }
    public static void main(String[] args) {
        System.out.println("Hello World");
        try{
            Auth a = new Auth("Server/runfiles/users");
            String s = a.authenticate("paulocorte", "password");
            System.out.println(s);
        } catch(Exception e) {
            System.out.println("Exception: "+e.getMessage());
            e.printStackTrace();
        }
    }
}