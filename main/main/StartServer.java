package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class StartServer {

	static String serverPort = "5647";
	
    /**
     * The server is booted to a thread using the runnable class for this file
     * It is used to launch the application only.
     * Prompts via textinput a port, and checks if it is in use. (todo)
     * 
     * @param	port
     * 
     * 
     *
     */
	public static void main(String[] args) {
		while (true) {
			try {
				System.out.println("Please enter a port to listen on (none-> 5647)");
				
				try {
					serverPort = (new BufferedReader(new InputStreamReader(System.in))).readLine();
				} catch (IOException e) {
					System.out.println("-> Autofill: 5647");
					serverPort = "5647";
					
				}
				
				if (serverPort.equals("")) {
					System.out.println("-> Autofill: 5647");
					serverPort = "5647";
				}
				
				Runnable r = new server.Server(Integer.parseInt(serverPort));
				
				Thread t = new Thread(r);
				t.start();
				t.join();
				
			} catch (IOException | NumberFormatException | InterruptedException e) {
				
				System.out.println("Server stopped unexpected... maybe try a different port ?");
				System.out.println("REBOOT...:");
				
			}
		}
		
		
	}

}
