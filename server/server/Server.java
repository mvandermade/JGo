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

import serverModel.ConnectedClientObj;
import serverModel.ConnectionManager;
import serverModel.ConnectionToServerObj;
import serverModel.GameManager;
import serverModel.PlayerManager;
import serverModel.ToClientPacket;
import serverModel.ToServerPacket;
//import serverView.GameServlet;
//import serverView.WaitingRoom;

public class Server implements Runnable {

	// Keep active
	private final ServerSocket serverSocket;
	// Storage
	private final ConnectionManager connMan;
	private final PlayerManager playMan;
	private final GameManager gameMan;
	
	private Queue<Socket> connQueue = new ConcurrentLinkedQueue<Socket>();
	private long pollQueueTime;
	
	// UTF16 escape chars
	private final String DELIMITER1 = "\\$";
	private final String DELIMITER2 = "\\_";
	

	
	//private final ExecutorService pool;
	
	// This is a method to manage different threads more efficiently (Executors)
	
	public Server(int port) throws IOException {
		
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
		pollQueueTime = 200;
		
		// Storage
		connMan = new ConnectionManager(serverSocket);
		
		// PlayerStorage
		
		playMan = new PlayerManager();
		
		// GameStorage
				
		gameMan = new GameManager();
		
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
			
			//System.out.print(".");
			
			servletQueue.forEach((c)->{

				// Here an object is made ToClientObject
				gameServlet(c);
				
			});
			
			
			//4. Packets are sent out async.
			// Maybe put script here which will trigger Tx ?
			connMan.transmitAllToClientQueue();
			
		} // end while true server loop
		
	} // end run

	private enum ClientCMDs {
		
		NAME, 
		MOVE,
		PASS,
		SETTINGS,
		QUIT,
		REQUESTGAME,
		RANDOM
		
	}
	
	private void gameServlet(ToServerPacket cRx) {
		// TODO Auto-generated method stub
		// This method answers the Rx packet ToSeverPacket with an Tx ToClientPacket.
		
		// Creating "outbox"
		List<ToClientPacket> outbox = new ArrayList<>();
		
		String clientCMD = cRx.getInputLine();
		int clientId = cRx.getClientId();
		// Expecting protocol 3
		// COMMAND$PAYLOAD
		// Check for it
		try {
			System.out.print("Client: ");
			System.out.print(clientId);
			String[] delimit1 = clientCMD.split(DELIMITER1);
			
			System.out.print(" delim1[0]: ");
			System.out.print(delimit1[0]);
			
			System.out.print(" delim1[1]: ");
			System.out.print(delimit1[1]);
			
			ClientCMDs clientCMDlist = ClientCMDs.valueOf(delimit1[0]);
			
			
			String[] delimit2 = delimit1[1].split(DELIMITER2);
			
			switch (clientCMDlist) {
			case NAME:
				playMan.addPlayer(clientId, delimit2[0]);
				outbox.add(new ToClientPacket(clientId, "HELLO, " +playMan.GetPlayerName(clientId)));
				break;
			case MOVE:
				break;
			case PASS:
				break;
			case SETTINGS:
				break;
			case QUIT:
				break;
			case REQUESTGAME:
				break;
			case RANDOM:
				break;
			default:
				outbox.add(new ToClientPacket(clientId, "UNKNOWNCOMMAND"));
				break;
			}
			
		} catch (IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
			
			outbox.add(new ToClientPacket(clientId, "UNKNOWNCOMMAND"));
			outbox.add(new ToClientPacket(clientId, "OTHER$TRY AGAIN"));
			e.printStackTrace();
		}
		
		List<ToClientPacket> txQueue = outbox.stream()
				.sorted((f1, f2) -> Long.compare(f1.getStartTime(), f2.getStartTime())).
                collect(Collectors.toList());
		
		txQueue.forEach((tx)->{

			// Here an object is made ToClientObject
			connMan.addToClientTxQueue(tx);
			
		});
		
		
		
		
		
	}


	
}
