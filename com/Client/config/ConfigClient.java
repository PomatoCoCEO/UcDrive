package com.Client.config;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.InputMismatchException;
import java.util.Scanner;

public class ConfigClient {

    private String primaryServerName, secondaryServerName;
    private int primaryServerPort, secondaryServerPort;
    private Scanner sc;

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

    private String getIpAddress (String promptMessage) {
        while(true) {
            try{
                System.out.print(promptMessage);
                String ip1 = sc.next();
                InetAddress.getByName(ip1);
                return ip1;
            } catch(UnknownHostException e) {
                System.out.println("You have entered an invalid address. A valid configuration is required.");
            }
        }

    }

    private int getPort(String promptMessage) {
        while(true) {
            try{
                System.out.println(promptMessage);
                int portNo = sc.nextInt();
                if(portNo <1024 || portNo > 49151) {
                    throw new InputMismatchException("Invalid value for port");
                }
                return portNo;
            } catch(InputMismatchException e) {
                System.out.println("Enter a number between 1024 and 49151");
            }
        }
    }

    public void configServerInfo() {

        

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
        setPrimaryServerName(getIpAddress("Enter new primary server ip: "));
        setPrimaryServerPort(getPort("Enter new primary server port: "));
        setSecondaryServerName(getIpAddress("Enter new secondary server ip: "));
        setSecondaryServerPort(getPort("Enter new secondary server port: "));

        this.saveInfo();

    }

    public void readConfig(String fileName) {
        try {

            File fr = new File(fileName);
            Scanner sc2 = new Scanner(fr);
            // BufferedReader br = new BufferedReader(fr);
            setPrimaryServerName(sc2.next());
            setPrimaryServerPort(sc2.nextInt());
            setSecondaryServerName(sc2.next());
            setSecondaryServerPort(sc2.nextInt());

        } catch (FileNotFoundException f) {
            System.out.println("File not found: " + f.getMessage());
            f.printStackTrace();
        }
    }

    public ConfigClient(String fileName) {
        sc = new Scanner(System.in);
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
