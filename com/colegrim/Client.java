package com.colegrim;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Client implements Runnable{
    private volatile UserList userList;
    private volatile List<Integer> usedPorts;
    private UI ui;
    private final int MINPORT = 10000;
    private final int MAXPORT = 60000;

    private InetAddress serverAddress;
    private int port;
    private String username;
    private boolean running;

    private int listeningPort;

    public Client(UI ui, InetAddress serverAddress, int port, String username){
        this.userList = new UserList();
        this.usedPorts = new ArrayList<Integer>();
        this.usedPorts.add(port);
        this.ui = ui;
        this.serverAddress = serverAddress;
        this.port = port;
        this.username = username;
        this.running = false;
    }

    @Override
    public void run() {
        this.running = true;
        startup();

        //while(running){
            try {
                ClientWorker cw = new ClientWorker();
                Thread cwThread = new Thread(cw);
                cwThread.start();
                sendMessage(new Message(Message.MessageType.Login, userList.getUser(username).toString()));
            }catch (Exception e){
                ui.displayError(e.getMessage());
            }
        //}
    }

    private void startup(){
        try {
            Socket s = new Socket(this.serverAddress, this.port);
            Connection connection = Handshakes.clientShake(s);

            BufferedReader br = connection.br;
            PrintWriter pw = connection.pw;

            pw.println(username);

            String userListString = Message.fromString(getNext(br)).getPayload();

            connection.close();

            this.userList = UserList.fromString(userListString);
            this.listeningPort = userList.getUser(username).getPort();
        }catch (Exception e){
            ui.displayError(e.getMessage());
        }

    }

    public UserList getUserlist(){
        return this.userList;
    }

    public List<Integer> getUsedPorts(){
        return this.usedPorts;
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

    public void sendMessage(Message m){
        for(User u : userList.getOnlineUsers()){
            if(u.getUsername() == this.username) continue;
            try {
                Socket s = new Socket(u.getAddress(), u.getPort());
                Connection connection = Handshakes.clientShake(s);
                PrintWriter pw = connection.pw;
                BufferedReader br = connection.br;
                pw.println(this.username);
                if(getNext(br).matches("GO")){
                    pw.println(m.toString());
                }
                connection.close();
            }catch (Exception e){
                ui.displayError("Trouble sending message to " + u.getUsername()
                    + ". Marking him as offline.");
                u.setOnline(false);
                sendMessage(new Message(Message.MessageType.LogOff, u.getUsername()));
            }
        }
    }

    public void readMessage(String source, Message m){
        if(m == null){
            ui.displayError("Had some trouble receiving a message. Ignoring. (null message)");
            return;
        }
        switch(m.getType()){
            case ChatMessage:
                ui.displayChatMessage(source, m.getPayload());
                return;
            case ListUpdate:
                try{
                    this.userList = UserList.fromString(m.getPayload());
                }catch(Exception e){
                    ui.displayError("Malformed userlist received. Ignoring.");
                }
                return;
            case LogOff:
                try {
                    this.userList.getUser(m.getPayload()).setOnline(false);
                }catch (Exception e){
                    ui.displayError("Malformed logoff received. Ignoring.");
                    return;
                }
                ui.displayNotification(source + " has logged off.");
                return;
            case ServerShutdown:
                ui.displayNotification("The server is going down. Users may no longer connect " +
                        "to the chat session. Normal chat may continue.");
                return;
            case Login:
                try {
                    User loggingIn = User.fromString(m.getPayload());
                    if(!this.userList.containsUser(loggingIn.getUsername())){
                        this.userList.addUser(loggingIn);
                    }
                    this.userList.getUser(loggingIn.getUsername()).setOnline(true);
                    ui.displayNotification(loggingIn.getUsername() + " has logged on.");
                }catch (Exception e){
                    e.printStackTrace();
                    ui.displayError("Malformed login received. Ignoring." + m.getPayload());
                    return;
                }
                return;
            default:
                ui.displayError("Bad message received.");
                return;
        }
    }

    public void cleanup(){
        sendMessage(new Message(Message.MessageType.LogOff, username));
    }

    private class ClientWorker implements Runnable{
        ServerSocket servSock;
        Connection connection;

        public ClientWorker(){
        }

        @Override
        public void run() {
            try {
                this.servSock = new ServerSocket(listeningPort);
                Socket s = servSock.accept();
                connection = Handshakes.servShake(s, usedPorts, MINPORT, MAXPORT);
            }catch (Exception e){
                e.printStackTrace();
                ui.displayError("Someone had trouble connecting to us.");
                return;
            }
            BufferedReader br = connection.br;
            PrintWriter pw = connection.pw;
            Message msg = null;
            String sourceName = null;
            try {
                sourceName = getNext(br);
                pw.println("GO");
                msg = Message.fromString(getNext(br));
                connection.close();
            }catch(Exception e){
                ui.displayError("Had some trouble receiving a message. Ignoring. (bad communication)");
            }
            readMessage(sourceName, msg);
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
