package com.colegrim;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * This is the main server/"tracker" functionality
 */
public class Server implements Runnable{
    private boolean running;
    private ServerSocket listeningSocket;
    private UI ui;
    private Client client;
    private final boolean ALLOWNEWUSERS = true;
    private final int MINPORT = 10000;
    private final int MAXPORT = 60000;

    public Server(Client client, int port){
        this.running = false;
        this.client = client;

        try {
            this.listeningSocket = new ServerSocket(port);
        }catch(Exception e){
            System.err.println("Couldn't bind to server socket! Quitting.");
            System.exit(1);
        }
    }

    /**
     * Attaches a UI. Same as with client, just here for modularity.
     *
     * @param ui UI to attach.
     */
    public void attachUI(UI ui){
        this.ui = ui;
    }

    @Override
    public void run() {
        this.running = true;
        ui.logMessage(1, "Server: Main thread started.");
        while(running){
            ui.logMessage(1, "Server: Entering main loop.");
            try {
                Socket s = this.listeningSocket.accept();
                try {
                    ui.logMessage(2, "Server: Got a connection, starting handshake.");
                    Connection connection = Handshakes.servShake(s, client.getUsedPorts(), MINPORT, MAXPORT);
                    ui.logMessage(2, "Server: Handshake completed. Starting handler thread.");
                    ServerWorker sw = new ServerWorker(connection);
                    Thread swThread = new Thread(sw);
                    swThread.start();
                }catch (Exception e){
                    ui.displayError(e.getMessage());
                }
            }catch(Exception e){
                ui.displayError(e.getMessage());
            }
            ui.logMessage(1, "Server: Main loop iteration completed.");
        }
    }

    /**
     * This is called when the server shuts down. We let all the clients know.
     */
    public void cleanup(){
        ui.logMessage(1, "Server: Entering cleanup.");
        try {
            client.sendMessage(new Message(Message.MessageType.ServerShutdown, "Bye."));
            this.listeningSocket.close();
        }catch (Exception e){

        }
    }

    /**
     * Worker Thread to handle incoming connections.
     *
     * // TODO: add ability to disable unknown users
     */
    private class ServerWorker implements Runnable{
        Connection connection;

        public ServerWorker(Connection connection){
            ui.logMessage(1, "Server: Worker created.");
            this.connection = connection;
        }

        @Override
        public void run() {
            ui.logMessage(1, "Server: Worker started.");
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
                ui.logMessage(1, "Server: Worker sending: ." + toSend.toString());
                pw.println(toSend.toString());
                connection.close();
            }catch (Exception e){
                ui.displayError(e.getMessage());
            }
            ui.logMessage(1, "Server: Worker job finished.");

        }

        // I should put this somewhere common
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
