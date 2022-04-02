package com.Server.auth;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.TreeSet;

import com.Server.Server;

public class Auth {
    private TreeSet<User> users;
    private Server server;

    public void loadUsers() throws IOException {

        Path p = Paths.get(server.getConfigPath(), "usr");

        try (DirectoryStream<Path> dStream = Files.newDirectoryStream(p)) {
            // String[] ans = dStream.
            for (Path filePath : dStream) {
                File fr = new File(filePath.toString());
                String absolutePath = server.getAbsolutePath();
                // this is in the server so it is ok

                try (Scanner sc = new Scanner(fr)) {
                    while (sc.hasNextLine()) {
                        String line1 = sc.nextLine();
                        String line2 = sc.nextLine();
                        String line3 = sc.nextLine();
                        String line4 = sc.nextLine();
                        // String[] sp = line.split(" ");
                        User u = new User(line1, line2, line3, line4);
                        users.add(u);

                        File f = new File(Paths.get(absolutePath, line1).toString());
                        if (f.mkdir() == true) { // there is no directory with that name
                            System.out.println("Directory has been created successfully " + line1);
                        } else {
                            System.out.println("Directory cannot be created (its already created) " + line1);
                        }

                    }
                } catch (FileNotFoundException f) {
                    System.out.println("File not found: " + f.getMessage());
                    f.printStackTrace();
                }
            }

        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    public enum Operation {
        DELETE, CHANGE, ADD
    }

    public synchronized void changeUsers(Operation op, User user) {
        User aid;
        switch (op) {
            case DELETE:
                users.remove(user);
                break;
            case CHANGE:
                aid = users.ceiling(new User(user.getUsername(), "", "", ""));
                if (aid.getUsername().equals(user.getUsername())) {
                    users.remove(aid);
                    users.add(user);
                }
                try {
                    String pathWrite = Paths.get(server.getConfigPath(), "usr", user.getUsername())
                            .toString();
                    BufferedWriter writer = new BufferedWriter(
                            new FileWriter(pathWrite));
                    writer.write(aid.toFileString());
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("changed");
                System.out.println(user);

                break;
            case ADD:
                // verify if user with that username already exists
                aid = users.ceiling(new User(user.getUsername(), "", "", ""));
                if (aid.getUsername().equals(user.getUsername())) {
                    users.remove(aid);
                    users.add(user);
                }
                // else throw an exception
                break;
        }
    }

    public void registerUser(String username, String password) {
        // ! falta obter a hash da password
        User user = new User(username, password, "", "");
        changeUsers(Operation.ADD, user);
    }

    public Auth(Server server) throws IOException {
        this.server = server;
        users = new TreeSet<>();
        loadUsers();
    }

    public User authenticate(String username, String password) throws Exception {
        User found = users.ceiling(new User(username, "", "", ""));
        if (!found.getUsername().equals(username) || !found.getPasswordHash().equals(password)
                || !found.getToken().equals("")) {
            throw new Exception("Authentication failed: user " + found);
        }

        found.setToken(username);
        changeUsers(Operation.CHANGE, found); // updates the information in the set, based on the username
        return found;
    }

    public User findUser(String username) {
        User found = users.ceiling(new User(username, "", "", ""));
        if (found.getUsername().equals(username))
            return found;
        else
            return null;
    }

    // public static void main(String[] args) {
    // System.out.println("Hello World");
    // try {
    // Auth a = new Auth("com/Server/runfiles/users");
    // String s = a.authenticate("paulocorte", "password");
    // System.out.println(s);
    // } catch (Exception e) {
    // System.out.println("Exception: " + e.getMessage());
    // e.printStackTrace();
    // }
    // }
}