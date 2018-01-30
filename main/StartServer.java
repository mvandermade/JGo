package jGoMain;

import java.io.IOException;

import server.Server;

public class StartServer {

	public static void main(String[] args) {
		// Use this file to start an instance of the server application.
		
		int port = 5647;
		
		// Run forever, if the server fails reboot
		while (true) {
			try {
				new Server(port);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			System.out.println("Rebooting server...");
		}

	}

}
