package com.colegrim;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Runnable{
    private boolean running;
    private ServerSocket listeningSocket;
    private UI ui;
    private Client client;
    private final boolean ALLOWNEWUSERS = true;
    private final int MINPORT = 10000;
    private final int MAXPORT = 60000;

    public Server(UI ui, Client client, int port){
        this.running = false;
        this.ui = ui;
        this.client = client;

        try {
            this.listeningSocket = new ServerSocket(port);
        }catch(Exception e){
            ui.displayError(e.getMessage());
        }
    }

    @Override
    public void run() {
        this.running = true;
        while(running){
            try {
                Socket s = this.listeningSocket.accept();
                try {
                    Connection connection = Handshakes.servShake(s, client.getUsedPorts(), MINPORT, MAXPORT);
                    ServerWorker sw = new ServerWorker(connection);
                    Thread swThread = new Thread(sw);
                    swThread.start();
                }catch (Exception e){
                    ui.displayError(e.getMessage());
                }
            }catch(Exception e){
                ui.displayError(e.getMessage());
            }
        }
    }

    public void cleanup(){
        client.sendMessage(new Message(Message.MessageType.ServerShutdown, "Bye."));
        try {
            this.listeningSocket.close();
        }catch (Exception e){

        }
    }

    private class ServerWorker implements Runnable{
        Connection connection;

        public ServerWorker(Connection connection){
            this.connection = connection;
        }

        @Override
        public void run() {
            try {
                BufferedReader br = connection.br;
                PrintWriter pw = connection.pw;

                String username = getNext(br);
                if(!client.getUserlist().containsUser(username)){
                    if(!ALLOWNEWUSERS) {
                        ui.displayError("Unknown user " + username + " tried to connect.");
                        return;
                    }
                    client.getUserlist().addUser(new User(username, connection.socket.getInetAddress(), connection.socket.getPort()));
                }
                client.getUserlist().getUser(username).setOnline(true);
                Message toSend = new Message(Message.MessageType.ListUpdate, client.getUserlist().toString());
                pw.println(toSend.toString());
                connection.close();
            }catch (Exception e){
                ui.displayError(e.getMessage());
            }

        }

        private String getNext(BufferedReader br) throws Exception{
            String input = null;
            while ((input = br.readLine()) == null) {
                try {
                    Thread.sleep(50);
                } catch (Exception e) {
                    // wake up
                }
            }
            return input;
        }
    }
}
