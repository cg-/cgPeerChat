/*
 * Project: Chat
 * Author: Cole Grim (cg-)
 * Created: 2/9/2015
 */

package com.colegrim;

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

        ui = new UI(username, client, server);
        Thread uiThread = new Thread(ui);
        client = new Client(ui, serverAddress, 4242, username);
        try {
            client.getUserlist().addUser(new User(username, serverAddress, 10000));
        }catch (Exception e){

        }

        Thread clientThread = new Thread(client);
        server = new Server(ui, client, 4242);
        Thread serverThread = new Thread(server);

        serverThread.start();
        clientThread.start();
        uiThread.start();
    }

    public static void cleanup(){
        ui.cleanup();
        server.cleanup();
        client.cleanup();
    }
}
