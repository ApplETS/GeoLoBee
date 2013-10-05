package com.etsmtl.ca;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.net.Socket;

public class main {
    private final String SERVER = "localhost";
    private final int PORT = 6697;
    private String username = "foobar";
    private String channel = "#foo";

    private BufferedWriter writer;
    private BufferedReader reader;
    private Socket socket;

    public static void main(String[] args) {
	new main();
    }

    private void connect () {
	try {
	    socket = new Socket (SERVER, PORT);
	    writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
	    reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

	    new Thread (new Reader ()).start();
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    private class Reader implements Runnable {
	public void run() {
	    String input = "";
	    try {
		while ((input = reader.readLine()) != null) {
		    System.out.println(input);
		}
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}
    }
}
