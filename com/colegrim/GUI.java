/*
 * Copyright 2015
 * Project: cgPeerChat
 * Author: Cole Grim (cg-)
 * Created: 2/13/2015
 */

package com.colegrim;

import javax.swing.*;
import java.awt.*;

public class GUI extends UI implements Runnable {
    String username;
    Client client;
    JFrame window;

    private JList userList;
    private JTextField inputBox;
    private JTextArea chatBox;
    private JPanel guiPanel;

    public GUI(String username, Client client){
        super(username, client);
        window = new JFrame("cgPeerChat");
        window.setContentPane(guiPanel);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.pack();
    }

    @Override
    public void run() {
        window.setVisible(true);
    }

    @Override
    public void displayError(String msg) {
        chatBox.append("\nerror: " + msg);
    }

    @Override
    public void displayNotification(String msg) {
        chatBox.append("\n\tnotification: " + msg);
    }

    @Override
    public void displayChatMessage(String user, String msg, AsciiColor color) {
        chatBox.append("\n" + user + ": " + msg);
    }

    @Override
    public void displayChatMessage(String user, String msg) {
        chatBox.append("\n" + user + ": " + msg);
    }
}
