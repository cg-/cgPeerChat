/*
 * Project: Chat
 * Author: Cole Grim (cg-)
 * Created: 2/10/2015
 */

package com.colegrim;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class Connection {
    Socket socket;
    BufferedReader br;
    PrintWriter pw;

    public Connection(Socket s) throws Exception{
        this.socket = s;
        this.br = new BufferedReader(new InputStreamReader(s.getInputStream()));
        this.pw = new PrintWriter(new OutputStreamWriter(s.getOutputStream()), true);
    }

    public void close() throws Exception{
        this.br.close();
        this.pw.close();
        this.socket.close();
    }
}
