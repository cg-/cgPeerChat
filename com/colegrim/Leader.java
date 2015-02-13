/*
 * Copyright 2015
 * Project: cgPeerChat
 * Author: Cole Grim (cg-)
 * Created: 2/10/2015
 */


package com.colegrim;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;

/**
 * Leader is the main entry point for a leader server client.
 */
public class Leader {
    static Server server;
    static Client client;
    static UI ui;

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run(){
                cleanup();
            }
        });

        String username = "SERVER";

        InetAddress serverAddress = null;
        try {
            serverAddress = InetAddress.getByName("localhost");
        }catch (Exception e){

        }

        int logging = 0;
        int listenPort = 4242;

        String filename = null;

        // parse args
        for(String s : args){
            String[] tokens = s.split("=");
            if(tokens.length != 2){
                System.err.println("Invalid option: " + s + "\nPlease read README.");
                System.exit(1);
            }
            String option = tokens[0];
            String value = tokens[1];
            if(option.toUpperCase().matches("USERNAME")){
                if(!value.matches("[a-zA-Z0-9]{1,15}")){
                    System.err.println("Username must only contain alphanumeric characters " +
                            "and be less than 15 characters long");
                    System.exit(1);
                }
                username = value;
            }else if(option.toUpperCase().matches("LOGGING")){
                if(!value.matches("[0-3]")){
                    System.err.println("Logging must be 0, 1, 2, or 3");
                    System.exit(1);
                }
                logging = Integer.parseInt(value);
            }else if(option.toUpperCase().matches("USERFILE")){
                filename = value;
            }else if(option.toUpperCase().matches("PORT")){
                if(!value.matches("[0-9]{1,5}")){
                    System.err.println("Invalid port!");
                    System.exit(1);
                }
                listenPort = Integer.parseInt(value);
            }else{
                System.err.println("Invalid option: " + s + "\nPlease read README.");
            }
        }

        // start everything up
        client = new Client(serverAddress, listenPort, username);
        server = new Server(client, listenPort);
        ui = new UI(username, client, server);
        ui.setLogginglevel(logging);
        client.attachUI(ui);
        server.attachUI(ui);
        Thread uiThread = new Thread(ui);

        // parse input file
        if(filename != null){
            parseFile(filename);
        }

        // add our server user if its not there
        if(!client.getUserlist().containsUser(username)) {
            try {
                client.getUserlist().addUser(new User(username, serverAddress, 10000));
            } catch (Exception e) {

            }
        }

        Thread clientThread = new Thread(client);
        Thread serverThread = new Thread(server);

        serverThread.start();
        clientThread.start();
        uiThread.start();

    }

    private static void parseFile(String filename){
        File f = new File(filename);
        // read file
        try {
            FileInputStream fis = new FileInputStream(f);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            String input = null;

            while ((input = br.readLine()) != null){    // until we read the whole file
                String[] words = input.split(" ");
                String username = null;
                InetAddress address = null;
                int port = -1;

                // remove whitespace that might exist
                for(String s : words){
                    s.trim();
                    if(s.isEmpty()) continue;
                    if(address == null) address = InetAddress.getByName(s);
                    else if(username == null) username = s;
                    else if(port == -1) port = Integer.valueOf(s);
                    else break;
                }

                // we expect file to be <IP Address> <Username> <Port> on new lines
                client.getUserlist().addUser(new User(username, address, port));
            }
            br.close();
            fis.close();
        }catch(Exception e){
            System.err.println(String.format("Couldn't read %s. Please ensure the file exists and is in"
                    + " the proper format. See README for details.", filename));
        }
    }

    /**
     * This is called when the program tries to exit
     */
    public static void cleanup(){
        ui.cleanup();
        client.cleanup();
        server.cleanup();
    }
}
