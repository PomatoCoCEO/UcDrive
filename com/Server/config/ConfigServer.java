package com.Server.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class ConfigServer {
    private int socketPort;

    public ConfigServer(String fileName) {
        File fr = new File(fileName);
        try (Scanner sc = new Scanner(fr)) {
            // BufferedReader br = new BufferedReader(fr);
            this.socketPort = sc.nextInt();
        } catch (FileNotFoundException f) {
            System.out.println("File not found: " + f.getMessage());
            f.printStackTrace();
        }
    }

    public int getSocketPort() {
        return socketPort;
    }

}
