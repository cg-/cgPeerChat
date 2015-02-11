package com.colegrim;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Client is the main handler of peer-to-peer communications
 */
public class Client implements Runnable{
    private volatile UserList userList;
    private volatile List<Integer> usedPorts;
    private UI ui;
    private final int MINPORT = 10000;
    private final int MAXPORT = 60000;

    private ServerSocket listeningSocket;
    private InetAddress serverAddress;
    private int port;
    private String username;
    private boolean running;

    public Client(InetAddress serverAddress, int port, String username){
        this.userList = new UserList();
        this.usedPorts = new ArrayList<Integer>();
        this.usedPorts.add(port);
        this.serverAddress = serverAddress;
        this.port = port;
        this.username = username;
        this.running = false;
    }

    @Override
    public void run() {
        ui.logMessage(1, "Client: Thread started.");
        this.running = true;
        startup();

        while(running){
            ui.logMessage(1, "Client: Entered main loop.");
            try {
                try {
                    Socket s = listeningSocket.accept();
                    ui.logMessage(2, "Client: Received connection. Beginning handshake.");
                    Connection connection = Handshakes.servShake(s, usedPorts, MINPORT, MAXPORT);
                    ui.logMessage(2, "Client: Handshake completed successfully.");
                    ClientWorker cw = new ClientWorker(connection);
                    Thread cwThread = new Thread(cw);
                    cwThread.start();
                }catch (Exception e){
                    e.printStackTrace();
                    ui.displayError("Someone had trouble connecting to us.");
                    return;
                }
            }catch (Exception e){
                ui.displayError(e.getMessage());
            }
            ui.logMessage(1, "Client: Loop iteration completed.");
        }
        ui.logMessage(1, "Client: Thread closing down.");
    }

    /**
     * We use this to attach our UI to the process. I did it this way so I could
     * easily add a GUI in the future.
     *
     * @param ui The UI
     */
    public void attachUI(UI ui){
        this.ui = ui;
    }

    /**
     * Startup stuff. We connect to the server and pull the userlist, then send everyone a login
     * message saying that we're online. We include our details in the login message.
     */
    private void startup(){
        ui.logMessage(2, "Client: Startup called.");
        try {
            ui.logMessage(1, "Client: Connecting to server.");
            Socket s = new Socket(this.serverAddress, this.port);
            ui.logMessage(1, "Client: Connected to server, starting handshake.");
            Connection connection = Handshakes.clientShake(s);
            ui.logMessage(1, "Client: Handshake finished.");

            BufferedReader br = connection.br;
            PrintWriter pw = connection.pw;

            pw.println(username);

            String userListString = Message.fromString(getNext(br)).getPayload();
            ui.logMessage(1, "Client: Got userliststring: " + userListString);

            connection.close();

            this.userList = UserList.fromString(userListString);
            int listeningPort = userList.getUser(username).getPort();
            ui.logMessage(1, "Client: Got listening port: " + String.valueOf(listeningPort));
            this.listeningSocket = new ServerSocket(listeningPort);
            Message toSend = new Message(Message.MessageType.Login, userList.getUser(username).toString());
            ui.logMessage(1, "Client: Created login message to send: " + toSend.toString());
            sendMessage(toSend);
            ui.logMessage(1, "Client: Startup finished.");
        }catch (Exception e){
            ui.displayError(e.getMessage());
        }

    }

    /**
     * Grabs the userlist. UI and Server both depend on this.
     *
     * @return Userlist.
     */
    public UserList getUserlist(){
        return this.userList;
    }

    /**
     * Grabs the used ports. Server depends on this.
     *
     * @return Used ports.
     */
    public List<Integer> getUsedPorts(){
        return this.usedPorts;
    }

    /**
     * I should put this somewhere common. Just waits for the next String from
     * the buffered reader (in the situation of waiting for a connected peer
     * to say something).
     *
     * @param br BufferedReader
     * @return String they said
     * @throws Exception if something goes wrong?
     */
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

    /**
     * Takes in a message, and sends it to everyone that we see as connected. If something
     * goes wrong, we assume that the client has lost connection, and we send a logoff message
     * to everyone letting them know.
     *
     * //todo: make people log themselves off if they get a logoff message with their name, or relogin
     *
     * @param m Message to send
     */
    public void sendMessage(Message m){
        ui.logMessage(1, "Client: Got a message to send: " + m.toString());
        for(User u : userList.getOnlineUsers()){
            if(u.getUsername().matches(this.username)) {
                ui.logMessage(1, "Client: Skipping (Because it's us): " + u.toString());
                continue;
            }
            ui.logMessage(1, "Client: Sending to: " + u.toString());
            try {
                Socket s = new Socket(u.getAddress(), u.getPort());
                ui.logMessage(1, "Client: Connected to " + u.getUsername());
                Connection connection = Handshakes.clientShake(s);
                ui.logMessage(1, "Client: Handshake done with " + u.getUsername());
                PrintWriter pw = connection.pw;
                BufferedReader br = connection.br;
                pw.println(this.username);
                if(getNext(br).matches("GO")){
                    pw.println(m.toString());
                }
                connection.close();
                ui.logMessage(1, "Client: Message sent to " + u.getUsername());
            }catch (Exception e){
                ui.displayError("Trouble sending message to " + u.getUsername()
                    + ". Marking him as offline.");
                u.setOnline(false);
                sendMessage(new Message(Message.MessageType.LogOff, u.getUsername()));
            }
            ui.logMessage(1, "Client: Done sending the message.");
        }
    }

    /**
     * Processes an incoming message and handles appropriately
     *
     * @param source Username we got the message from (as determined during handshake)
     * @param m Message to process
     */
    public void readMessage(String source, Message m){
        ui.logMessage(1, "Client: Reading a message from " + source + " with contents: " + m.toString());
        if(m == null){
            ui.displayError("Had some trouble receiving a message. Ignoring. (null message)");
            return;
        }
        switch(m.getType()){
            case ChatMessage:
                int colorNum = 0;
                for(String s : userList.getStringArray()){
                    if (s.matches(source)) break;
                    colorNum++;
                }
                UI.AsciiColor c = UI.AsciiColor.values()[colorNum % UI.AsciiColor.values().length];
                ui.displayChatMessage(source, m.getPayload(), c);
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

    /**
     * This is called when the program shuts down. Sends a logoff message.
     */
    public void cleanup(){
        ui.logMessage(1, "Client: Cleanup called.");
        sendMessage(new Message(Message.MessageType.LogOff, username));
    }

    /**
     * A worker thread to handle incoming connections.
     */
    private class ClientWorker implements Runnable{
        Connection connection;

        public ClientWorker(Connection c){
            ui.logMessage(1, "Client: New ClientWorker created.");
            this.connection = c;
        }

        @Override
        public void run() {
            ui.logMessage(1, "Client: ClientWorker started.");
            BufferedReader br = connection.br;
            PrintWriter pw = connection.pw;
            Message msg = null;
            String sourceName = null;
            try {
                sourceName = getNext(br);
                ui.logMessage(1, "Client: ClientWorker connected with: " + sourceName);
                pw.println("GO");
                msg = Message.fromString(getNext(br));
                ui.logMessage(1, "Client: ClientWorker got Message: " + msg.toString());
                connection.close();
            }catch(Exception e){
                ui.displayError("Had some trouble receiving a message. Ignoring. (bad communication)");
            }
            readMessage(sourceName, msg);
            ui.logMessage(1, "Client: ClientWorker finished.");
        }

        // I should put this somewhere common.
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
