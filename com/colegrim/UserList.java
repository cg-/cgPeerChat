/*
 * Copyright 2015
 * Project: cgPeerChat
 * Author: Cole Grim (cg-)
 * Created: 2/10/2015
 */


package com.colegrim;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * A list of users
 */
public class UserList {
    private List<User> list;
    public final int VERSION = 1;

    // Constructor
    public UserList(){
        this.list = new ArrayList<User>();
    }

    /**
     * Returns a User object from a username
     *
     * @param s The username
     * @return User
     * @throws Exception If the user isn't found
     */
    public User getUser(String s) throws Exception{
        for(User u : list){
            if(u.getUsername().matches(s)) return u;
        }
        throw new Exception("User not found.");
    }

    /**
     * Checks if a User is in the UserList from a provided username
     *
     * @param s The username
     * @return true if the user is in the list, false otherwise
     */
    public boolean containsUser(String s){
        for(User u : list){
            if(u.getUsername().matches(s)) return true;
        }
        return false;
    }

    /**
     * Adds a User to the list
     *
     * @param user A User to add
     * @throws Exception If the username is already in the list
     */
    public void addUser(User user) throws Exception{
        if(this.containsUser(user.getUsername())) throw new Exception("User already exists!");
        this.list.add(user);
    }

    /**
     * Returns an array of Strings with the usernames of all the users in the list.
     *
     * @return String array of usernames
     */
    public String[] getStringArray(){
        String[] toReturn = new String[list.size()];

        int i = 0;
        for(User u : list){
            toReturn[i] = u.getUsername();
            i++;
        }

        return toReturn;
    }

    /**
     * Returns a list with all the online users.
     *
     * @return List with online Users.
     */
    public List<User> getOnlineUsers(){
        List<User> toReturn = new ArrayList<User>();
        for(User u : list){
            if(u.isOnline()) toReturn.add(u);
        }
        return toReturn;
    }

    /**
     * Generates a UserList from a properly formatted String.
     *
     * The String should be formatted as follows:
     * <userstring>#<userstring>#...
     *
     * For information on userstrings, see User.fromString()
     *
     * @param s The properly formatted string
     * @return A UserList
     * @throws Exception If the String was not properly formatted
     */
    public static UserList fromString(String s) throws Exception{
        UserList toReturn = new UserList();
        String[] tokens = s.split("#");
        for(String userstring : tokens){
            toReturn.addUser(User.fromString(userstring));
        }
        return toReturn;
    }

    /**
     * Generates a properly formatted String for a UserList.
     *
     * See fromString for formatting information.
     *
     * @return A properly formatted String
     */
    @Override
    public String toString(){
        StringWriter sw = new StringWriter();
        int i = 0;

        for(User u : list){
            sw.write(u.toString());
            if(i != list.size()-1) sw.write("#");
        }
        return sw.toString();
    }
}
