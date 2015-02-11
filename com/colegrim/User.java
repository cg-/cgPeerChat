/*
 * Copyright 2015
 * Project: cgPeerChat
 * Author: Cole Grim (cg-)
 * Created: 2/10/2015
 */


package com.colegrim;

import java.net.InetAddress;

/**
 * Represents a chat user.
 */
public class User {
    public final int VERSION = 1;
    InetAddress address;
    int port;
    String username;
    boolean online;

    // Constructors
    public User(String username, InetAddress address, int port){
        this.username = username;
        this.address = address;
        this.port = port;
        this.online = false;
    }

    public User(String username, InetAddress address, int port, boolean state){
        this.username = username;
        this.address = address;
        this.port = port;
        this.online = state;
    }

    // Getters
    public String getUsername(){
        return this.username;
    }

    public InetAddress getAddress(){
        return this.address;
    }

    public int getPort(){
        return this.port;
    }

    /**
     * Changes the state of a User
     *
     * @param state true if online, false if offline
     */
    public void setOnline(boolean state){
        this.online = state;
    }

    /**
     * Checks if a user is online
     *
     * @return true if online, false if offline
     */
    public boolean isOnline(){
        return this.online;
    }

    /**
     * Generates a user from a properly formatted String.
     *
     * Strings should be formatted as follows:
     * <username>;<address>;<port>
     *     -or-
     * <username>;<address>;<port>;<state>
     *
     * toString will format a user correctly.
     *
     * @param s username string to process
     * @return A User object
     * @throws Exception If the string was invalid
     */
    public static User fromString(String s) throws Exception{
        String[] tokens = s.split(";");
        String username = tokens[0];
        InetAddress address;
        try {
            address = InetAddress.getByName(tokens[1]);
        }catch (Exception e){
            throw new Exception("Invalid userstring: " + s);
        }
        int port = Integer.parseInt(tokens[2]);
        if(tokens.length == 3) return new User(username, address, port);
        if(tokens.length == 4) return new User(username, address, port, Boolean.parseBoolean(tokens[3]));
        throw new Exception("Invalid userstring: " + s);
    }

    /**
     * Generates a properly formatted userstring from a user.
     *
     * Strings will be formatted as follows:
     * <username>;<address>;<port>;<state>
     *
     * fromString will generate a User from this String.
     *
     * @return properly formatted userstring
     */
    @Override
    public String toString(){
        return String.format("%s;%s;%s;%s",
                this.username,
                this.address.getHostAddress(),
                String.valueOf(this.port),
                String.valueOf(this.online));
    }
}
