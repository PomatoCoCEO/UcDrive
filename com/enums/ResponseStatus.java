package com.enums;

public enum ResponseStatus {
    OK("OK"),
    BAD_REQUEST("Bad Request"),
    INTERNAL_SERVER_ERROR("Internal Server Error"),
    UNAUTHORIZED("Unauthorized");

    ResponseStatus(String status) {
        this.status = status;
    }

    private String status;

    public String getStatus() {
        return this.status;
    }

}