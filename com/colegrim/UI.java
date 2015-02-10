/*
 * Project: Chat
 * Author: Cole Grim (cg-)
 * Created: 2/9/2015
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
        RESET("\u001B[0m"), BLACK("\u001B[30m"), RED("\u001B[31m"), GREEN("\u001B[32m"),
        YELLOW("\u001B[33m"), BLUE("\u001B[34m"), PURPLE("\u001B[35m"), CYAN("\u001B[36m"),
        WHITE("\u001B[37m");

        final String code;

        AsciiColor(String code){
            this.code = code;
        }

        @Override
        public String toString(){
            return this.code;
        }
    }

    private boolean running;
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
                if(isValidCommand(input)){
                    executeCommand(getCommand(input));
                }else{
                    displayError("Invalid command entered!");
                }
            }
        }catch(Exception e){
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
        System.out.println("Currently connected users:");
        for(String s : client.getUserlist().getStringArray()){
            System.out.println(s);
        }
    }

    /**
     * Shows an error to the user
     *
     * @param msg Error message to display
     */
    public void displayError(String msg){
        System.err.println("error: " + msg);

    }

    /**
     * Shows a notification to the user
     *
     * @param msg Notification to display
     */
    public void displayNotification(String msg){
        System.out.println("notification: " + msg);
    }

    /**
     * Shows a chat message
     *
     * @param user Username of sender
     * @param msg Message
     */
    public void displayChatMessage(String user, String msg){
        System.out.println(user + ": " + msg);
    }

    /**
     * Shows a chat message with color
     *
     * @param user Username of sender
     * @param msg Message
     * @param color AsciiColor of color to display message in
     */
    public void displayChatMessage(String user, String msg, AsciiColor color){
        System.out.println(color.code + user + ": " + msg + AsciiColor.RESET);
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
}
