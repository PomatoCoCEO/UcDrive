package com.Client.config;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class ConfigClient {

    private String primaryServerName, secondaryServerName;
    private int primaryServerPort, secondaryServerPort;

    public void saveInfo() {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("com/Client/config/config"));

            writer.write(primaryServerName + " " + primaryServerPort + "\n" + secondaryServerName + " "
                    + secondaryServerPort + "\n");
            writer.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void configServerInfo() {

        Scanner sc = new Scanner(System.in);

        boolean config = false;

        while (true) {
            System.out.println("Do you want to configure server info? y/n");

            String resp = sc.next();
            switch (resp) {
                case "y":
                case "Y":
                    config = true;
                    break;
                case "n":
                case "N":
                    return;
                default:
                    System.out.println("Wrong command. Try again");
            }

            if (config)
                break;
        }

        // BufferedReader br = new BufferedReader(fr);
        // ! verify ip syntax
        System.out.println("Enter new primary server ip: ");
        setPrimaryServerName(sc.next());
        System.out.println("Enter new primary server port: ");
        setPrimaryServerPort(sc.nextInt());
        System.out.println("Enter new secondary server ip: ");
        setSecondaryServerName(sc.next());
        System.out.println("Enter new secondary server port: ");
        setSecondaryServerPort(sc.nextInt());

        this.saveInfo();

    }

    public void readConfig(String fileName) {
        try {

            File fr = new File(fileName);
            Scanner sc = new Scanner(fr);
            // BufferedReader br = new BufferedReader(fr);
            setPrimaryServerName(sc.next());
            setPrimaryServerPort(sc.nextInt());
            setSecondaryServerName(sc.next());
            setSecondaryServerPort(sc.nextInt());

        } catch (FileNotFoundException f) {
            System.out.println("File not found: " + f.getMessage());
            f.printStackTrace();
        } catch (IOException e) {
            System.out.println("close:" + e.getMessage());
        }
    }

    public ConfigClient(String fileName) {

        readConfig("com/Client/config/config");
        configServerInfo();

    }

    public int getSecondaryServerPort() {
        return secondaryServerPort;
    }

    public void setSecondaryServerPort(int secondaryServerPort) {
        this.secondaryServerPort = secondaryServerPort;
    }

    public int getPrimaryServerPort() {
        return primaryServerPort;
    }

    public void setPrimaryServerPort(int primaryServerPort) {
        this.primaryServerPort = primaryServerPort;
    }

    public String getSecondaryServerName() {
        return secondaryServerName;
    }

    public void setSecondaryServerName(String secondaryServerName) {
        this.secondaryServerName = secondaryServerName;
    }

    public String getPrimaryServerName() {
        return primaryServerName;
    }

    public void setPrimaryServerName(String primaryServerName) {
        this.primaryServerName = primaryServerName;
    }

}
