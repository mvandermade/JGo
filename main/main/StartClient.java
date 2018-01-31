package main;

import client.Client;

public class StartClient {

    /**
     * The client is booted.
     * It is used to launch the application only.
     * Prompts via textinput a port, and checks if it is in use. (todo)
     * 
     * @param	port		Port of the server to connect to
     * @param	hostname	hostname/IP of the server.
     * 
     * 
     *
     */
	public static void main(String[] args) {
		// Use this file to start one instance of the client application.
		// Defaults
//		int port = 5647;
//		String hostname = "localhost";
		
		new Client();
	}

}