package server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import serverModel.ConnectionManager;
import serverView.GameServlet;
import serverView.WaitingRoom;

public class Server implements Runnable {

	private final ServerSocket serverSocket;
	private Queue<Socket> connQueue = new ConcurrentLinkedQueue<Socket>();
	private long pollQueueTime;
	
	//private final ExecutorService pool;
	
	// This is a method to manage different threads more efficiently (Executors)
	
	public Server(int port, int poolSize) throws IOException {
		
		serverSocket = new ServerSocket(port);
		
		// Anonymous function that runs forever
		// Adds sockets to connQueue concurrently
		( new Thread() { public void run() {
			// do something 
				while (true) {
					
					try {
						connQueue.add(serverSocket.accept());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
			} } ).start(); 
		
		
		// Setting
		pollQueueTime = 1500;
	}
	
	// SERVER THREAD
	public void run() {
		
		
		
		// While loop logic, server is allowed to sleep when idling
		while (true) {
			try {
				Thread.sleep(pollQueueTime);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			System.out.println("polling...");
			
			// Java9 this can be improved with a nullchecker
			Socket nextConnInLine = connQueue.poll();
			if (nextConnInLine != null) {
				System.out.println(nextConnInLine);
			}
			
		}
		
	}
	
}
