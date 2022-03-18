package com.Server.except;

public class AuthorizationException extends Exception {
    public AuthorizationException() {
        super();
    }

    public AuthorizationException(String s) {
        super(s);
    }
}