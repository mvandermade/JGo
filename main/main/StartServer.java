package main;

import java.io.IOException;

public class StartServer {

	static int port = 5647;
	
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
		// TODO Auto-generated method stub
		try {
			
			Runnable r = new server.Server(port);
			
			Thread t = new Thread(r);
			t.start();
			
		} catch (IOException e) {
			
			System.out.println("Server stopped unexpected...:");
			e.printStackTrace();
			System.out.println("REBOOT MANUALLY...:");
			
		}
		
		
	}

}
