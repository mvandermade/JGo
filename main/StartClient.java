package jGoMain;

import client.Client;

public class StartClient {

	public static void main(String[] args) {
		// Use this file to start one instance of the client application.
		
		int port = 5647;
		String hostname = "localhost";
		
		new Client(hostname, port);
	}

}
