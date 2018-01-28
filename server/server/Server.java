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
	
	private final Queue<Socket> connQueue = new ConcurrentLinkedQueue<Socket>();
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
						// Blocking
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
		
		// PlayerStorage
		
		playMan = new PlayerManager();
		
		// GameStorage
				
		
		// gameMan will be requiring some players.
		gameMan = new GameManager(playMan);
		
		// Storage
		// playMan is passed because of access to removal functions on errors.
		// gameMan is there to trigger any other issues on removal of player
		
		//gameMan also passes its ToClient message queue to connMan
		connMan = new ConnectionManager(serverSocket, gameMan, playMan);
		

		
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
			
			
			
			servletQueue.forEach((c)->{

				// Here an object is made ToClientObject
				System.out.println("GOT..........");
				System.out.println(c.getInputLine());
				gameServlet(c);
				System.out.println("...........OK");
				
				
			});
			
			
			
			
			
			
			//4. Packets are sent out async.
			// Maybe put script here which will trigger Tx ?
			// Not concurrent
			connMan.transmitAllToClientQueue();
			
		} // end while true server loop
		
	} // end run

	private enum ClientCMDs {
		
		NAME, 
		MOVE,
		SETTINGS,
		QUIT,
		REQUESTGAME,
		LOBBY,
		CHAT
		
	}
	
	private void gameServlet(ToServerPacket cRx) {
		// TODO Auto-generated method stub
		// This method answers the Rx packet ToSeverPacket with an Tx ToClientPacket.
		
		// Expecting protocol 3
		// COMMAND$PAYLOAD
		// Check for it
		
		// Creating "outbox" for this client (for visualisation purposes)
		
		List<ToClientPacket> outbox = new ArrayList<>();
		
		// Try to strip out the message block
		
		try {
			
			String inputLineCMD = cRx.getInputLine();
			int clientId = cRx.getClientId();
		
			String[] inputLineSplit = inputLineCMD.split("\\"+getDELIMITER1());
			ClientCMDs clientCMDEnumVal = ClientCMDs.valueOf(inputLineSplit[0]);
			// These commands only allowed if a NAME is set
			if (null != playMan.getPlayerName(clientId)) {
				
				// if (false) .. not in game
				if (playMan.getPlayerObj(clientId).getIsInGame()) {
					
					playerIsInGameServlet(cRx, outbox, clientCMDEnumVal, inputLineSplit);
					
				} else {
					
					playerNotInGameServlet(cRx, outbox, clientCMDEnumVal, inputLineSplit);
				}
				
				playerGeneralServlet(cRx, outbox, clientCMDEnumVal, inputLineSplit);
				
			} else {
				
				playerAskNameServlet(cRx, outbox, clientCMDEnumVal, inputLineSplit);
			}
			
		} catch (NullPointerException | IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
			
			outbox.add(new ToClientPacket(cRx.getClientId(), "ERROR",cRx.getInputLine()+" +Not in my list of commands: NAME, MOVE, SETTINGS, QUIT, REQUESTGAME, LOBBY, CHAT."));
			e.printStackTrace();
		}
		
		// Put all messages in queue. Will be sent out in step 4 alltogether in main.
		outbox.stream()
				.sorted((f1, f2) -> Long.compare(f1.getStartTime(), f2.getStartTime()))
                .collect(Collectors.toList())
                .forEach((toClientPacket)->{
                	// Here an object is made ToClientObject (not yet sent)
                	connMan.addToClientTxQueue(toClientPacket);
		});
		
		System.out.println("\n^cmd processed" + new java.util.Date());
		
	}
	
	private void playerAskNameServlet(ToServerPacket cRx, List<ToClientPacket> outbox, ClientCMDs clientCMD,
			String[] inputLineSplit) {
		// The NAME command is always allowed
		
		int clientId = cRx.getClientId();
		String inputLineCMD = cRx.getInputLine();
		
		switch (clientCMD) {
		case NAME:
			
			try {
			
				String[] payload = inputLineSplit[1].split("\\"+getDELIMITER2());
				final String payloadNAME = payload[0];
				
				long duplicateNames = playMan.getListOfAllPlayers().stream()
				.filter((player) -> {
					return player.getName().equals(payloadNAME);
				}).count();
				
				if (duplicateNames==0) {
					playMan.addPlayer(clientId, payloadNAME);
					outbox.add(new ToClientPacket(clientId, "CHAT","SERVER"+DELIMITER1+"Warm welcome to: " +playMan.getPlayerName(clientId) + "!"));
					outbox.add(new ToClientPacket(clientId, "CHAT","SERVER"+DELIMITER1+"May I suggest to use: REQUESTGAME, SETTINGS, LOBBY, CHAT &&ingame: MOVE, QUIT."));
					

				} else {
					outbox.add(new ToClientPacket(clientId, "ERROR","Name already taken try again:"+inputLineCMD));

				}
				
			} catch (ArrayIndexOutOfBoundsException e) {
				
				e.printStackTrace();
				outbox.add(new ToClientPacket(clientId, "ERROR","Enter name after:"+inputLineCMD));

			}
			
			//System.out.print(" delim2[1]: "); System.out.print(delimit2[1]);
				
			break;
			
		default:
			
			outbox.add(new ToClientPacket(clientId, "UNKNOWNCMD",""));
			
		} // switch NAME, default
	}

	private void playerGeneralServlet(ToServerPacket cRx, List<ToClientPacket> outbox, ClientCMDs clientCMD,
			String[] inputLineSplit) {
		// TODO Auto-generated method stub
		int clientId = cRx.getClientId();
		
		
		// This part can only be accessed if ClientPlayerName exists
		try {
			switch (clientCMD) {
			case SETTINGS:
				// You can change this anytime, Before or after invoking RequestGame.
				// Settings will be leading only if P1 position.
				try {
					playMan.setColourOf(clientId, inputLineSplit[1]);
					
					try {
						//inputLineSplit[2];
						playMan.setBoardSizeOf(clientId, inputLineSplit[2]);
						
					} catch (ArrayIndexOutOfBoundsException e) {
						
						outbox.add(new ToClientPacket(clientId, "ERROR","Check arg[2]:BoardSize: SETTINGS$"+inputLineSplit[1]+"$<ERROR>"));

					}
					
				} catch (ArrayIndexOutOfBoundsException e) {
					outbox.add(new ToClientPacket(clientId, "ERROR","Check arg[1]:Colour: SETTINGS$<ERROR>"));
				}
				
				outbox.add(new ToClientPacket(clientId, "OTHER","Your P1 Settings: Colour: "+playMan.getColourOf(clientId)+" Boardsize:" + playMan.getBoardSizeOf(clientId)));

				break;
			case LOBBY:
				outbox.add(new ToClientPacket(clientId, "OTHER","Others connected to server: "+playMan.getListOfAllOtherPlayers(clientId).size()+". List of not-ingame:"));
				
				if (playMan.getListOfAllOtherPlayers(clientId).size() == 0) {
					
					outbox.add(new ToClientPacket(clientId, "CMDHINT","REQUESTGAME$2$RANDOM"));
					outbox.add(new ToClientPacket(clientId, "LOBBY",""));
					
				} else {
					
					String otherPlayersReply = playMan.getListOfAllOtherPlayers(clientId)
				            .stream()
				            .filter(playerObj -> !playerObj.getIsInGame())
				            .map(playerObj -> playerObj.getName()+DELIMITER2)
				            .collect(Collectors.joining());
					
					outbox.add(new ToClientPacket(clientId, "LOBBY",otherPlayersReply));
					
					outbox.add(new ToClientPacket(clientId, "CMDHINT",">REQUESTGAME$2$RANDOM"));
				}
				
				break;
			case CHAT:
				String[] payload = inputLineSplit[1].split("\\"+getDELIMITER2());
				String payloadCHAT = payload[0];
				String chatSender = playMan.getPlayerName(clientId);
				
				if(playMan.getListOfAllOtherPlayers(clientId).size() == 0) {
					
					outbox.add(new ToClientPacket(clientId, "ERROR","No players in lobby..."));

					
				} else {
					playMan.getListOfAllOtherPlayers(clientId).stream().forEach(
						op -> {
							outbox.add(new ToClientPacket(op.getClientId(), "CHAT","FROM"+DELIMITER1+chatSender+DELIMITER1+payloadCHAT));
						}
					);
					
				}
				break;
			default:
				// TODO: say something, but not double the error
				break;
			}
			
		} catch (IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
			
				outbox.add(new ToClientPacket(clientId, "ERROR", "UNKNOWNCOMMAND"));
				outbox.add(new ToClientPacket(clientId, "ERROR", "TRY AGAIN"));
				e.printStackTrace();
		}
		
	}

	private void playerIsInGameServlet(ToServerPacket cRx, List<ToClientPacket> outbox, ClientCMDs clientCMD,
			String[] inputLineSplit) {
		// TODO Auto-generated method stub
		int clientId = cRx.getClientId();
		
		
		// This part can only be accessed if ClientPlayerName exists and IsInGame
		try {
			switch (clientCMD) {
			case MOVE:
				
				int moveDataRow = 0;
				int moveDataCol = 0;
				
				try {
					
					// To decrypt: MOVE%1_2
					String[] moveData = inputLineSplit[1].split("\\"+getDELIMITER2());
					
					if(moveData[0].equals("PASS")) {
						
						gameMan.passFor(clientId);
						
					} else {
					
						moveDataRow = Integer.parseInt(moveData[0]);
						moveDataCol = Integer.parseInt(moveData[1]);
						
						if (moveDataRow < 1 || moveDataRow > gameMan.getGameObjForClient(clientId).getBoardSize() || moveDataCol < 1 || moveDataRow > gameMan.getGameObjForClient(clientId).getBoardSize()) {
							
							// Here already apply the rule to correct for starting at 0 instead of 1 (TUI)
							moveDataRow = moveDataRow - 1;
							moveDataCol = moveDataCol - 1;
							// Send message
							gameMan.getGameObjForClient(clientId).messageClientId(clientId, "ERROR", "MOVE NOT ALLOWED, OUT OF BOUNDS");
							
						} else {
							
							moveDataRow = moveDataRow - 1;
							moveDataCol = moveDataCol - 1;
							gameMan.tryMoveFor(clientId, moveDataRow, moveDataCol);
							
						}
					}

				} catch (NullPointerException | ArrayIndexOutOfBoundsException | NumberFormatException e) {
					
					e.printStackTrace();
					outbox.add(new ToClientPacket(clientId, "ERROR","MOVE$row(int)_col(int)"));

				}
				
				break;
			case QUIT:
				outbox.add(new ToClientPacket(clientId, "OTHER","Quitting..."));
				gameMan.quit2PGameFor(clientId);
				break;
			default:
				// TODO: say something, but not double the error
				break;
			}
			
		} catch (IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
			
				outbox.add(new ToClientPacket(clientId, "ERROR", "UNKNOWNCOMMAND"));
				outbox.add(new ToClientPacket(clientId, "ERROR", "TRY AGAIN"));
				e.printStackTrace();
		}
	}

	private void playerNotInGameServlet(ToServerPacket cRx, List<ToClientPacket> outbox, ClientCMDs clientCMD, String[] inputLineSplit) {
		// TODO Auto-generated method stub
		try {
			int clientId = cRx.getClientId();
			
			switch (clientCMD) {
			case REQUESTGAME:
				// Example what to expect: REQUESTGAME$<int players>$<string against>
				try {
					// Challenge
					//int amountOfPlayers= Integer.parseInt(inputLineSplit[1]);
					//String playingAgainst= inputLineSplit[2];
					
					gameMan.addToRequestQueue(cRx);
					
					// The queue will be processed by another step "the game manager"
					gameMan.processRequestQueue();
					
				} catch(IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
					outbox.add(new ToClientPacket(clientId, "ERROR","RequestGame error"));
				}				
				break;
			default:
				break;
			}
			
		} catch (IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
			outbox.add(new ToClientPacket(cRx.getClientId(), "ERROR","RequestGame error. Try again"));
		}

	}

	public static String getDELIMITER2() {
		return DELIMITER2;
	}

	public static String getDELIMITER1() {
		return DELIMITER1;
	}


	
}
