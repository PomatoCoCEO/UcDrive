package com.DataTransfer;

import java.io.Serializable;

public class Request implements Serializable {

    private String message;
    private String token;

    public Request(String message) {
        this.message = message;
        token = "";
    }

    public Request(String message, String token) {
        this.message = message;
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "Request [message=" + message + "]";
    }

}