/*
 * Copyright 2015
 * Project: cgPeerChat
 * Author: Cole Grim (cg-)
 * Created: 2/10/2015
 */


package com.colegrim;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Random;

/**
 * A simple handshake to negotiate a port to communicate on so the server can go back
 * to listening.
 */
public class Handshakes{
    public static final int SLEEPTIME = 50;
    public static final int MAXTRIES = 1000;
    public static final int SOCKETWAIT = 100;

    /**
     * Conducts a handshake for a server.
     *
     * @param s Original socket with currently connected client. This will be closed during the handshake.
     * @param usedPorts A list of Integers with currently used ports. This will be modified.
     * @param min The minimum port to suggest.
     * @param max The maximum port to suggest.
     * @return A connection to the client on a new port.
     * @throws Exception If something goes wrong.
     */
    public static Connection servShake(Socket s, List<Integer> usedPorts, int min, int max) throws Exception{
        BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(s.getOutputStream()), true);

        if (!getNext(br).matches("HELLO")) throw new Exception("Bad handshake! Stage 1 Server");

        boolean serverOkayed = false;
        int suggestedPort = -1;
        while(!serverOkayed) {
            boolean usingPort = true;
            while(usingPort){
                suggestedPort = getSuggestedPort(usedPorts, min, max);
                if(checkPort(suggestedPort)) usingPort = false;
            }

            pw.println(String.valueOf(suggestedPort));
            if(getNext(br).matches("OK")) serverOkayed = true;
        }
        br.close();
        pw.close();
        s.close();

        ServerSocket servSock = new ServerSocket(suggestedPort);
        Socket newSock = servSock.accept();

        Connection toReturn = new Connection(newSock);

        pw = toReturn.pw;
        br = toReturn.br;

        pw.println("HELLO");
        if (!getNext(br).matches("HI")) throw new Exception("Bad handshake! Stage 2 Server");

        usedPorts.add(suggestedPort);

        return toReturn;
    }

    /**
     * Conducts a handshake from the client.
     *
     * @param s Current connection to the server.
     * @return A socket with a connection to the server on a new port.
     * @throws Exception
     */
    public static Connection clientShake(Socket s) throws Exception{
        InetAddress serverAddress = s.getInetAddress();
        BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(s.getOutputStream()), true);

        pw.println("HELLO");

        boolean gotGoodPort = false;

        int newPort = -1;
        while(!gotGoodPort) {
            newPort = Integer.parseInt(getNext(br));
            if (checkPort(newPort)) gotGoodPort = true;
            else pw.println("NO");
        }
        pw.println("OK");

        br.close();
        pw.close();
        s.close();

        Thread.sleep(SOCKETWAIT); // Give the socket a bit to open up.

        Socket newSock = new Socket(serverAddress, newPort);

        Connection toReturn = new Connection(newSock);

        pw = toReturn.pw;
        br = toReturn.br;

        if(!getNext(br).matches("HELLO")) throw new Exception("Bad Handshake! Stage 1 Client");
        pw.println("HI");

        return toReturn;
    }

    private static String getNext(BufferedReader br) throws Exception{
        String input = null;
        while ((input = br.readLine()) == null) {
            try {
                Thread.sleep(SLEEPTIME);
            } catch (Exception e) {
                // wake up
            }
        }
        return input;
    }

    private static int getSuggestedPort(List<Integer> list, int min, int max) throws Exception{
        Random r = new Random();
        int tries = 0;
        int toReturn = r.nextInt(max-min+1) + min-1;
        while(list.contains(toReturn)){
            toReturn = r.nextInt(max-min+1) + min-1;
            if(tries++ > MAXTRIES) {
                throw new Exception("Invalid parameters given to getSuggestedPort (or all the ports are used)");
            }
        }
        return toReturn;
    }

    /**
     * Checks is a port is available.
     *
     * Thanks to TwentyMiles on SO
     * http://stackoverflow.com/users/92937/twentymiles
     *
     * @param port
     * @return
     */
    private static boolean checkPort(int port) {
        try {
            Socket toCheck = new Socket("localhost", port);
            return false; // something is listening on this port.
        } catch (IOException e) {
            return true;  // its available
        }
    }

}
