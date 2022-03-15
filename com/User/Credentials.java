package com.User;
import java.io.Serializable;


public class Credentials implements Serializable{

    private String username, password;

    public Credentials(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String loginString() {
        return "LOGIN USER "+username+" PASSWORD "+password;
    }

    @Override
    public String toString() {
        return "Auth [password=" + password + ", username=" + username + "]";
    }
    
}