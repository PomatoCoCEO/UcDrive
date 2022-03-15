package com.DataTransfer;


import java.io.Serializable;


public class Reply implements Serializable{

    private String message;
    private String statusCode;

    public Reply(String message, String statusCode) {
        this.message = message;
        this.statusCode = statusCode;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    

    @Override
    public String toString() {
        return "Auth [message=" + message + "]";
    }
    
}