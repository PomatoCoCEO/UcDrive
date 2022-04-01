package com.Server.auth;

public class User implements Comparable {
    private String username;
    private String passwordHash;
    private String serverDir;
    private String clientDir;
    private String token;

    public User(String username, String passwordHash, String serverDir, String clientDir) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.serverDir = serverDir;
        this.setClientDir(clientDir);
        this.token = "";
    }

    public String getClientDir() {
        return clientDir;
    }

    public void setClientDir(String clientDir) {
        this.clientDir = clientDir;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getServerDir() {
        return serverDir;
    }

    public void setServerDir(String serverDir) {
        this.serverDir = serverDir;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return "<" + username + ">\n<" + passwordHash + ">\n<" + serverDir + ">\n<" + clientDir + ">";
    }

    public String toFileString() {
        return  username + "\n" + passwordHash + "\n" + serverDir + "\n" + clientDir +"\n";
    }

    @Override
    public int compareTo(Object o) {
        // TODO Auto-generated method stub
        if (!(o instanceof User))
            throw new ClassCastException("Invalid object cast");
        User u = (User) o;
        return this.username.compareTo(u.username);
    }

}
