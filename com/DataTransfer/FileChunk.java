package com.DataTransfer;

import java.io.Serializable;

public class FileChunk implements Serializable {
    private byte[] bytes;

    public FileChunk(byte[] bytes) {
        this.bytes = bytes;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public int getNumBytes() {
        return bytes.length;
    }
}