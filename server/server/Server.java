package server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import serverModel.ConnectionManager;
import serverModel.ConnectionToServerObj;
import serverModel.ToClientPacket;
import serverModel.ToServerPacket;
//import serverView.GameServlet;
//import serverView.WaitingRoom;

public class Server implements Runnable {

	private final ServerSocket serverSocket;
	private final ConnectionManager connMan;
	private Queue<Socket> connQueue = new ConcurrentLinkedQueue<Socket>();
	private long pollQueueTime;
	
	//private final ExecutorService pool;
	
	// This is a method to manage different threads more efficiently (Executors)
	
	public Server(int port, int poolSize) throws IOException {
		
		serverSocket = new ServerSocket(port);
		
		// Anonymous function that runs forever
		// Adds sockets to connQueue concurrently
		System.out.println("Booting acceptor thread");
		
		(new Thread() {
			public void run() {
			// do something 
				while (true) {
					
					try {
						connQueue.add(serverSocket.accept());
						
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}).start(); // Boot the thread
		
		System.out.println("OK");
		
		// Setting
		pollQueueTime = 500;
		
		// Storage
		connMan = new ConnectionManager(serverSocket);
		
		// Constructor ready... it is assumed now .run() is invoked by other launcher application
	}
	
	// SERVER THREAD
	public void run() {
		
		// While loop logic. 
		// Server run thread is allowed to sleep when idling
		while (true) {
			try {
				Thread.sleep(pollQueueTime);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//System.out.print("zzz");
			
			// 1. Check for new clients, and add them to the chain
			// 2. The clients are automatically assigned an inputStream
			
			// Java9 this can be improved with a nullchecker
			Socket nextConnInLine = connQueue.poll();
			
			if (nextConnInLine != null) {
				try {
					connMan.addNewClient(nextConnInLine);
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			// 3. For each client: get the list of prepared incoming objects via again concurrency
			
			// Get all buffers and references to their ports
			
			// Temp vars
			List<ToServerPacket> localPolledQueue = new ArrayList<ToServerPacket>();
			
			Queue<ToServerPacket> toServerPacketQueue = connMan.getToServerQueue();
			
			Boolean done = false;
			
			while(!done) {
				
				//System.out.print("poll");
				
				ToServerPacket polledObject = toServerPacketQueue.poll();
				
				//System.out.print(polledObject);
				
				if (polledObject != null) {
					
					localPolledQueue.add(polledObject);
					
				} else {
					
					done = true;
				}
			}
			
			// Synchronize using timestamping. So output can be done in order of array.
			List<ToServerPacket> servletQueue = localPolledQueue.stream()
					.sorted((f1, f2) -> Long.compare(f1.getStartTime(), f2.getStartTime())).
                    collect(Collectors.toList());
			
			System.out.print(".");
			
			servletQueue.forEach((c)->{
				System.out.println(c.getInputLine());
				connMan.transmitToClient(c.getClientId(), "----------------> Ok");
				
				// Now let everyone know
				connMan.getClients().forEach((others)->{
					if (others.getClientId() != c.getClientId()) {
						connMan.transmitToClient(others.getClientId(), c.getClientId() + "):    "+c.getInputLine());
					}
				}); // message all
				
			}); //transmit to Client
			
			
		} // end while
		
	} // end run


	
}
