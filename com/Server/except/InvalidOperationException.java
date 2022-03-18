package com.Server.except;

public class InvalidOperationException extends Exception {
    public InvalidOperationException() {
    }

    public InvalidOperationException(String s) {
        super(s);
    }
}