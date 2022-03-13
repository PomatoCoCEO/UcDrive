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
    private TreeSet<User> users;
    public void loadUsers(String fileName) throws IOException {
        File fr = new File(fileName);
        try (Scanner sc = new Scanner(fr)) {
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

    public enum Operation {
        DELETE, CHANGE, ADD
    }

    public synchronized void changeUsers(Operation op, User user) {
        User aid;
        switch(op) {
            case DELETE:
                users.delete(user);
                break;
            case CHANGE:
                aid = users.ceiling(new User(user.username, "", ""));
                if(aid.username.equals(user.username)){
                    users.delete(aid);
                    users.add(user);
                }
                break;
            case ADD:
                // verify if user with that username already exists
                aid = users.ceiling(new User(user.username, "", ""));
                if(aid.username.equals(user.username)){
                    users.delete(aid);
                    users.add(user);
                }
                // else throw an exception
                break;
        }
    }

    public void registerUser(String username, String password) {
        //! falta obter a hash da password
        User user = new User(username, password, "");
        synchronized(users) {
            users.add(user);
        }
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