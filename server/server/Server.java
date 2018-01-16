package server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import serverView.GameServlet;
import serverView.WaitingRoom;

public class Server implements Runnable {

	private final ServerSocket serverSocket;
	//private final ExecutorService pool;
	private int clientCounter = 0;
	
	// This is a method to manage different threads more efficiently (Executors)
	
	public Server(int port, int poolSize) throws IOException {
		
		serverSocket = new ServerSocket(port);
		//pool = Executors.newFixedThreadPool(poolSize);
		//pool.execute((new WaitingRoom(serverSocket, clientCounter)));
		//pool.shutdown();
	}
	
	// Because this is runnable service runs
	
	//Temporarily used
	WaitingRoom newRoom = null;
	public void run() {
		while (true) {
			try {
				System.out.println("Host addr: " + InetAddress.getLocalHost().getHostName()); 
				
					System.out.println(LocalDateTime.now());
					clientCounter = clientCounter + 1;
					System.out.println("SERVER     : WaitingRoomThread#:");
					System.out.println(clientCounter);
					
					// Blocking 
					newRoom = new WaitingRoom(serverSocket, clientCounter);
					
					
				} catch (IOException e) {
					e.printStackTrace();
					
				}
			
			// OK start the servlet and detach it using Threading
				
			Runnable r = new GameServlet(newRoom.getWaitingPlayers());
			
			System.out.println("flushing");
			System.out.flush();
			Thread t = new Thread(r);
			System.out.println("stepped over");
			
			//These are non-blocking and will throw their own exceptions later on
			t.start();
		
		}
		
	}
	
}
