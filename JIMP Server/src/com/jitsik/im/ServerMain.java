package com.jitsik.im;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.jitsik.im.BuddyList.BuddyListManager;
import com.jitsik.im.Database.*;

public class ServerMain {
	public final static int listenPort = 1338;
	
	public static void main (String[] args) {
		try {
			AccountDatabaseManager.initializeDatabase();
			BuddyListManager.initializeDatabase();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		ServerSocket server = null;
		try {
		    server = new ServerSocket(listenPort);
		} catch (IOException e) {
		    System.out.println("Could not listen on port: " + listenPort);
		    System.exit(-1);
		}
		
		Log.debugLevel(Log.LEVEL_ALL);
		
		Log.log(Log.LEVEL_EVENTS, "Listening on port: " + listenPort);
		
		while (true) {
			Socket clientSocket = null;
			try {
			    clientSocket = server.accept();
			} catch (IOException e) {
				Log.log(Log.LEVEL_ERROR, "Accept failed: " + listenPort);
			    System.exit(-1);
			}
			
			ServerSession session = new ServerSession(new ObjectsOverTCPConnection(clientSocket));
			Thread thread = new Thread(session);
			thread.start();
			Log.log(Log.LEVEL_EVENTS, "Connection established");
		}
		
	}
}
