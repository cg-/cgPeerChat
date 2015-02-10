/*
 * Project: Chat
 * Author: Cole Grim (cg-)
 * Created: 2/9/2015
 */

package com.colegrim;

import java.net.InetAddress;

/**
 * Peer is the main entry point for a normal chat peer client.
 */
public class Peer {
    static Client client;
    static UI ui;

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run(){
                cleanup();
            }
        });
        String username = "temp";

        InetAddress serverAddress = null;
        try {
            serverAddress = InetAddress.getByName("localhost");
        }catch (Exception e){

        }


        ui = new UI(username, client);
        Thread uiThread = new Thread(ui);
        client = new Client(ui, serverAddress, 4242, username);
        Thread clientThread = new Thread(client);
        uiThread.start();
        clientThread.start();

    }

    public static void cleanup(){
        ui.cleanup();
        client.cleanup();
    }
}
