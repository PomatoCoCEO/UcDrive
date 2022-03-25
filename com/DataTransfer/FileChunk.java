package com.DataTransfer;

public class FileChunk {
    private byte[] bytes;
    public FileChunk(byte[] bytes) {
        this.bytes = bytes;
    }
    public byte[] getBytes() {
        return bytes;
    }
}