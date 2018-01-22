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
	private final static String DELIMITER1 = "$";
	private final static String DELIMITER2 = "_";
	

	
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
		RANDOM,
		LOBBY,
		CHAT
		
	}
	
	private void gameServlet(ToServerPacket cRx) {
		// TODO Auto-generated method stub
		// This method answers the Rx packet ToSeverPacket with an Tx ToClientPacket.
		
		// Expecting protocol 3
		// COMMAND$PAYLOAD
		// Check for it
		
		// Creating "outbox"
		List<ToClientPacket> outbox = new ArrayList<>();
		
		String inputLineCMD = cRx.getInputLine();
		int clientId = cRx.getClientId();
		
		// Try to strip out the message block
		
		try {
		
			System.out.print("Client: "); System.out.print(clientId); System.out.print(inputLineCMD);
			String[] inputLineSplit = inputLineCMD.split("\\"+getDELIMITER1());
			ClientCMDs clientCMD = ClientCMDs.valueOf(inputLineSplit[0]);
			
			switch (clientCMD) {
			case NAME:
				
				String[] payload = inputLineSplit[1].split("\\"+getDELIMITER2());
				String payloadNAME = payload[0];
				
				//System.out.print(" delim2[1]: "); System.out.print(delimit2[1]);
				playMan.addPlayer(clientId, payloadNAME);
				
				outbox.add(new ToClientPacket(clientId, "OTHER","Welcome, " +playMan.GetPlayerName(clientId) + "."));
				outbox.add(new ToClientPacket(clientId, "CMDHINT","LOBBY, REQUESTGAME$2$RANDOM"));
				break;
				
			default:
				
				try {
					
					String ClientPlayerName = playMan.GetPlayerName(clientId);
					clientCMDServlet(cRx, outbox, clientCMD, inputLineSplit);
					
				} catch(NullPointerException e) {
					
					outbox.add(new ToClientPacket(clientId, "CMDHINT","NAME$yourname"));
					
				} // Try getName
				
			} // switch NAME, default
			
		} catch (IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
			
			outbox.add(new ToClientPacket(clientId, "CMDHINT","UNKNOWN:"+inputLineCMD));

		}
		
		List<ToClientPacket> txQueue = outbox.stream()
				.sorted((f1, f2) -> Long.compare(f1.getStartTime(), f2.getStartTime())).
                collect(Collectors.toList());
		
		txQueue.forEach((tx)->{

			// Here an object is made ToClientObject
			connMan.addToClientTxQueue(tx);
			
		});
		
	}

	private void clientCMDServlet(ToServerPacket cRx, List<ToClientPacket> outbox, ClientCMDs clientCMD, String[] inputLineSplit) {
		// TODO Auto-generated method stub
		
		int clientId = cRx.getClientId();
		String ClientPlayerName = playMan.GetPlayerName(clientId);
		
		// This part can only be accessed if ClientPlayerName exisits
		if (ClientPlayerName != null) {
			try {
				switch (clientCMD) {
				case MOVE:
					break;
				case PASS:
					break;
				case SETTINGS:
					break;
				case QUIT:
					break;
				case REQUESTGAME:
					// Example what to expect: REQUESTGAME$<int players>$<string against>
					try {
						int amountOfPlayers= Integer.parseInt(inputLineSplit[1]);
						String playingAgainst= inputLineSplit[2];
						
						// Create empty game with the player in it.
					} catch(IllegalArgumentException | ArrayIndexOutOfBoundsException e) {					
						outbox.add(new ToClientPacket(clientId, "CMDHINT","REQUESTGAME$2$RANDOM"));
					}
					
					// Ignore this for now...
					
					break;
				case RANDOM:
					break;
				case LOBBY:
					outbox.add(new ToClientPacket(clientId, "OTHER","#other players in game: "+playMan.GetListOfAllOtherPlayers(clientId).size()));
					
					if (playMan.GetListOfAllOtherPlayers(clientId).size() == 0) {
						
						outbox.add(new ToClientPacket(clientId, "CMDHINT","REQUESTGAME$2$RANDOM"));
						
					} else {
						String otherPlayersReply = playMan.GetListOfAllOtherPlayers(clientId)
					            .stream()
					            .map(playerObj -> "P: " + playerObj.getName()+", ")
					            .collect(Collectors.joining());
						
						outbox.add(new ToClientPacket(clientId, "OTHER",otherPlayersReply));
						
						outbox.add(new ToClientPacket(clientId, "CMDHINT","REQUESTGAME$2$RANDOM"));
	
						outbox.add(new ToClientPacket(clientId, "CMDHINT","REQUESTGAME$2$"+playMan.GetListOfAllOtherPlayers(clientId).get(0).getName()));
					}
					break;
				case CHAT:
					String[] payload = inputLineSplit[1].split("\\"+getDELIMITER2());
					String payloadCHAT = payload[0];
					String chatSender = playMan.GetPlayerName(clientId);
					
					if(playMan.GetListOfAllOtherPlayers(clientId).size() == 0) {
						
						outbox.add(new ToClientPacket(clientId, "ERROR","No players in lobby..."));
	
						
					} else {
						playMan.GetListOfAllOtherPlayers(clientId).stream().forEach(
							op -> {
								outbox.add(new ToClientPacket(op.getClientId(), "CHAT","FROM"+DELIMITER1+chatSender+DELIMITER1+payloadCHAT));
							}
						);
						
					}
					
					break;
				default:
					outbox.add(new ToClientPacket(clientId, "ERROR", "UNKNOWNCOMMAND"));
					break;
				}
				
			} catch (IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
				
					outbox.add(new ToClientPacket(clientId, "ERROR", "UNKNOWNCOMMAND"));
					outbox.add(new ToClientPacket(clientId, "ERROR", "TRY AGAIN"));
					e.printStackTrace();
			}
		} // endif ClientPlayerName
		
	}

	public static String getDELIMITER2() {
		return DELIMITER2;
	}

	public static String getDELIMITER1() {
		return DELIMITER1;
	}


	
}
