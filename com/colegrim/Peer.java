/*
 * Copyright 2015
 * Project: cgPeerChat
 * Author: Cole Grim (cg-)
 * Created: 2/10/2015
 */

package com.colegrim;

import java.net.InetAddress;
import java.util.Random;

/**
 * Peer is the main entry point for a normal chat peer client.
 */
public class Peer {
    static Client client = null;
    static UI ui = null;

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run(){
                if(client != null && ui != null) {
                    cleanup();
                }
            }
        });

        Random r = new Random();
        String username = "GuestUser" + String.valueOf(r.nextInt(50000));

        InetAddress serverAddress = null;
        try {
            serverAddress = InetAddress.getByName("localhost");
        }catch (Exception e){

        }

        int serverPort = 4242;
        int logging = 0;

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
            }else if(option.toUpperCase().matches("SERVER")){
                try {
                    serverAddress = InetAddress.getByName(value);
                }catch (Exception e){
                    System.err.println("Couldn't parse address from: " + value);
                    System.exit(1);
                }
            }else if(option.toUpperCase().matches("PORT")){
                if(!value.matches("[0-9]{1,5}")){
                    System.err.println("Invalid port!");
                    System.exit(1);
                }
                serverPort = Integer.parseInt(value);
            }else{
                System.err.println("Invalid option: " + s + "\nPlease read README.");
            }
        }

        // Start everything up.
        client = new Client(serverAddress, serverPort, username);
        ui = new UI(username, client);
        ui.setLogginglevel(logging);
        client.attachUI(ui);
        Thread uiThread = new Thread(ui);
        Thread clientThread = new Thread(client);
        uiThread.start();
        clientThread.start();
    }

    /**
     * This is called when the program tries to close.
     */
    public static void cleanup(){
        ui.cleanup();
        client.cleanup();
    }
}
