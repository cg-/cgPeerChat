/*
 * Copyright 2015
 * Project: cgPeerChat
 * Author: Cole Grim (cg-)
 * Created: 2/10/2015
 */

package com.colegrim;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * This is a temporary basic text UI. Will use the methods later.
 */
public class UI implements Runnable{
    private enum Command{
        LIST("list"), QUIT("quit");

        final String cmdString;

        Command(String cmdString){
            this.cmdString = cmdString;
        }

        @Override
        public String toString(){
            return this.cmdString;
        }
    }

    public enum AsciiColor{
        RESET("\u001B[0m", "<\\font>"), RED("\u001B[31m", "<font color=\"red\">"), GREEN("\u001B[32m", "<font color=\"green\">"),
        YELLOW("\u001B[33m", "<font color=\"yellow\">"), BLUE("\u001B[34m", "<font color=\"blue\">"), PURPLE("\u001B[35m", "<font color=\"purple\">"),
        CYAN("\u001B[36m", "<font color=\"cyan\">");

        final String code;
        final String html;

        AsciiColor(String code, String html){
            this.code = code;
            this.html = html;
        }

        @Override
        public String toString(){
            return this.code;
        }
    }

    private boolean running;
    private int logginglevel;
    private boolean leader;
    private String username;
    private Client client;
    private Server server;

    // Constructors
    public UI(String username, Client client){
        this.running = false;
        this.username = username;
        this.client = client;
        this.leader = false;
    }

    public UI(String username, Client client, Server server){
        this.running = false;
        this.username = username;
        this.client = client;
        this.server = server;
        this.leader = true;
    }


    @Override
    public void run() {
        running = true;
        while(running){
            displayPrompt();
            processCommand();
        }
    }

    /**
     * Reads in a command from the user, verifies it, and then executes it.
     */
    private void processCommand(){
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String input = null;
        try {
            while ((input = br.readLine()) != null) {
                if(!(input.charAt(0) == '/')){
                    Message toSend = new Message(Message.MessageType.ChatMessage, input);
                    client.sendMessage(toSend);
                    displayPrompt();
                }else {
                    input = input.substring(1);
                    if (isValidCommand(input)) {
                        executeCommand(getCommand(input));
                    } else {
                        displayError("Invalid command entered!");
                    }
                }
            }
        }catch(Exception e){
            e.printStackTrace();
            displayError("Trouble processing a command: " + input);
        }
    }

    /**
     * Executes a command.
     *
     * @param c Enum representing a command.
     */
    private void executeCommand(Command c){
        switch(c){
            case LIST:
                displayUserlist();
                break;
            case QUIT:
                System.exit(0);
                break;
            default:
                System.err.println("executeCommand encountered an invalid command.");
                return;
        }
    }

    /**
     * Takes in a string and makes sure it's a valid command.
     *
     * @param command String to check
     * @return if the command is valid.
     */
    private boolean isValidCommand(String command) {
        for(Command c : Command.values()){
            if(c.toString().matches(command)) return true;
        }
        return false;
    }

    /**
     * Gets the enum of a command from the string.
     *
     * @param s String of the command
     * @return Enum of the command
     */
    private Command getCommand(String s) throws Exception{
        for(Command c : Command.values()){
            if(c.toString().matches(s)) return c;
        }
        throw new Exception("Command not found: " + s);
    }

    /**
     * Grabs the userlist from the client and displays it.
     */
    public void displayUserlist(){
        System.out.println("\nCurrently connected users:");
        for(User u : client.getUserlist().getOnlineUsers()){
            System.out.println("* " + u.getUsername());
        }
        System.out.println("");
        displayPrompt();
    }

    /**
     * Shows an error to the user
     *
     * @param msg Error message to display
     */
    public void displayError(String msg){
        System.err.println(AsciiColor.RED + "\n\terror: " + msg + AsciiColor.RESET);
        displayPrompt();
    }

    /**
     * Shows a notification to the user
     *
     * @param msg Notification to display
     */
    public void displayNotification(String msg){
        System.out.println(AsciiColor.CYAN + "\n\tnotification: " + msg + AsciiColor.RESET);
        displayPrompt();
    }

    /**
     * Shows a chat message
     *
     * @param user Username of sender
     * @param msg Message
     */
    public void displayChatMessage(String user, String msg){
        System.out.println("\n\t" + user + ": " + msg);
        displayPrompt();
    }

    /**
     * Shows a chat message with color
     *
     * @param user Username of sender
     * @param msg Message
     * @param color AsciiColor of color to display message in
     */
    public void displayChatMessage(String user, String msg, AsciiColor color){
        System.out.println("\n\t" + color.code + user + ": " + msg + AsciiColor.RESET);
        displayPrompt();
    }

    public void displayNewUser(String username){
        System.out.println("\n\t" + username + " has joined.");
        displayPrompt();
    }

    public void setUserList(String[] names){
    }

    /**
     * Shows a prompt
     */
    private void displayPrompt(){
        System.out.printf("%s: ", username);
    }

    /**
     * Cleans up when program is shut down.
     */
    public void cleanup(){
        displayNotification("Shutting down.");
    }

    /**
     * Sets the level of log to display. 0 is off. 1 is all, 2 is some, 3 is only important.
     *
     * @param i log level to display
     */
    public void setLogginglevel(int i){
        this.logginglevel = i;
    }

    /**
     * Shows a log message.
     *
     * @param severity severity (1 lowest, 3 highest)
     * @param msg message to display
     */
    public void logMessage(int severity, String msg){
        if(logginglevel >= severity) {
            System.out.println("\nlog: " + msg);
            displayPrompt();
        }
    }
}
