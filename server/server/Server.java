package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

import serverController.ConnectionManager;
import serverController.GameManager;
import serverController.PlayerManager;
import serverView.ToClientPacket;
import serverView.ToServerPacket;

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
	
    /**
     * The server has as job to make sure all attached clients are managed.
     * 1) join the server on the clientAcceptor();
     * 2) Register commands inbound to this server PC
     * 3) Process commands via servlets
     * 4) Send out messages via network.
     * 
     * @param	port	integer number of the port to open the socket listner service on.
     * @return	void
     */
	
	public Server(int port) throws IOException {
		
		// New server socket
		serverSocket = new ServerSocket(port);
		
		// Anonymous function that runs forever
		System.out.println("Booting clientAcceptorThread");
		(new Thread() {
			public void run() {
				clientAcceptor();
			}
		}).start(); // Boot the thread
		
		System.out.println("OK");
		
		// Setting the 'refresh rate' of this class of the non-concurrent part.
		pollQueueTime = 50;
		
		// Controller of players. This final instance also holds all data in memory.
		playMan = new PlayerManager();
		
		// Controller of games. This final instance also holds all data in memory.
		gameMan = new GameManager(playMan);
		
		// Storage of the connections. This final instance also holds all data in memory.
		connMan = new ConnectionManager(serverSocket, gameMan, playMan);
		

		
		// Constructor ready... it is assumed now .run() is invoked by other launcher application
	}


	
	// SERVER THREAD
    /**
     * Runnable thread part of the server. Designed to loop forever with use of while(true).
     * pollingTime is fixed
     * 
     * 
     * @param	
     * @param	
     * @param 	
     * @return	
     * 
     * @see 
     */
	public void run() {
		
		// While loop logic. 
		// Server run thread is allowed to sleep when idling
		while (true) {
			try {
				Thread.sleep(pollQueueTime);
			} catch (InterruptedException e) {
				//e.printStackTrace();
			}
			
			// 1. see if ClientAcceptor() has produced any new entries.
			Socket nextConnInLine = connQueue.poll();
			
			if (nextConnInLine != null) {
				try {
					connMan.addNewClient(nextConnInLine);
					
				} catch (IOException e) {
					//e.printStackTrace();
				}
			}
			
			// 2. For each client: get the list of prepared incoming objects
			// and pass them to the servlet
			pollPacketsAndOfferToServlet();
						
			//4. Packets are sent out using the connectionManager instance
			connMan.transmitAllToClientQueue();
			
		} // end while true server loop
		
	} // end run
	
	
	/**
	 * 
	 */
	private void clientAcceptor() {
	// do something 
		while (true) {
			
			try {
				// Blocking
				connQueue.add(serverSocket.accept());
				
			} catch (IOException e) {
				// do nothing
			}
		}
	}

	/**
	 * Grab packet objects directed towards the server from any connected client.
	 * These packets are stored in the ConnectionManager instance.
	 * Pass those subsequently to gameServlet()
	 * 
	 * @param	
	 * @return	void
	 * @see gameServlet()
	 * 
	 */
	
	private void pollPacketsAndOfferToServlet() {
		List<ToServerPacket> localPolledQueue = new ArrayList<ToServerPacket>();
		
		Queue<ToServerPacket> toServerPacketQueue = connMan.getToServerQueue();
		
		Boolean done = false;
		
		while (!done) {
			
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

		servletQueue.forEach((c) -> {
			// Here an object is made ToClientObject
			System.out.println("GOT..........");
			System.out.println(c.getInputLine());
			gameServlet(c);
			System.out.println("...........OK");
		});
	}
	
	/**
	 * An enum with the possible commands the server is expecting.
	 * No flexibility built in.
	 */

	private enum ClientCMDs {
		
		NAME, 
		MOVE,
		SETTINGS,
		QUIT,
		REQUESTGAME,
		LOBBY,
		CHAT
		
	}
	
	
	/**
	 * The GameServlet's task is to decide if the command the user sent is valid.
	 * First the enum is checked if the command is allowed.
	 * Afterward it is checked if the user can actually invoke a certain command.
	 * 
	 * e.g. some commands aren't allowed or make no sense to do.
	 * The user is then notified via textual messages such as error or info. (non standard).
	 * For telnet clients for example these messages may be useful.
	 * 
	 * @param	toServerPacket package captured from a client.
	 * @return	void
	 * @see 
	 * 
	 */
	
	private void gameServlet(ToServerPacket toServerPacket) {	
		// Expecting protocol 5
		// COMMAND$PAYLOAD
		// Check for pattern
		
		// Creating "outbox" for this client
		
		List<ToClientPacket> outbox = new ArrayList<>();
		
		// Try to strip out the message block
		
		try {
			
			String inputLineCMD = toServerPacket.getInputLine();
			int clientId = toServerPacket.getClientId();
		
			String[] inputLineSplit = inputLineCMD.split("\\" + getDELIMITER1());
			ClientCMDs clientCMDString = ClientCMDs.valueOf(inputLineSplit[0]);
			// These commands only allowed if a NAME is set
			if (null != playMan.getPlayerName(clientId)) {
				
				// if (false) .. not in game
				if (playMan.getPlayerObj(clientId).getIsInGame()) {
					
					playerIsInGameServlet(toServerPacket, outbox, clientCMDString, inputLineSplit);
					
				} else {
					
					playerNotInGameServlet(toServerPacket, outbox, clientCMDString, inputLineSplit);
				}
				
				playerGeneralServlet(toServerPacket, outbox, clientCMDString, inputLineSplit);
				
			} else {
				
				playerAskNameServlet(toServerPacket, outbox, clientCMDString, inputLineSplit);
			}
			
		} catch (NullPointerException | IllegalArgumentException |
				ArrayIndexOutOfBoundsException e) {
			
			// Notify the user
			outbox.add(new ToClientPacket(toServerPacket.getClientId(),
					"ERROR",
					toServerPacket.getInputLine() + 
					" +Not in my list of commands:"
					+ " NAME, MOVE, SETTINGS, QUIT, REQUESTGAME, LOBBY, CHAT."));
		} finally {
		
			// Put all messages in queue. Will be sent out in step 4 alltogether in main.
			outbox.stream()
					.sorted((f1, f2) -> Long.compare(f1.getStartTime(), f2.getStartTime()))
	                .collect(Collectors.toList())
	                .forEach((toClientPacket) -> {
	                	// Here an object is made ToClientObject (not yet sent)
	                	connMan.addToClientTxQueue(toClientPacket);
	                });
			
			System.out.println("^cmd processed,"
					+ " data sent to client: " + toServerPacket.getClientId()
					+ ">" + playMan.getPlayerName(toServerPacket.getClientId()) + "< "
					+ " at: " + new java.util.Date());
		}
	}
	
	/**
	 * Ask for the player's name, if not provided the user is not kicked.
	 * Instead a message is sent to the user to provide a name.
	 * The only option remaining however is then this servlet for the user.
	 * Also checks if name already is taken by someone connected to the server.
	 * 
	 * @param	toServerPacket 	package captured from a client.
	 * @param	outbox			to allow adding to this array of outbound packets
	 * @param	clientCMD		the enum containing all available commands
	 * @param	inputLineSplit	the split inputline
	 * @return	void
	 * @see 	gameservlet()
	 * 
	 */
	
	private void playerAskNameServlet(ToServerPacket toServerPacket, List<ToClientPacket> outbox,
			ClientCMDs clientCMD, String[] inputLineSplit) {
		// The NAME command is always allowed
		int clientId = toServerPacket.getClientId();
		String inputLineCMD = toServerPacket.getInputLine();
		
		switch (clientCMD) {
			case NAME:	
				try {
					String[] payload = inputLineSplit[1].split("\\" + DELIMITER2);
					final String payloadNAME = payload[0];
					
					long duplicateNames = playMan.getListOfAllPlayers().stream()
							.filter((player) -> {
								return player.getName().equals(payloadNAME);
							}).count();
					
					if (duplicateNames == 0) {
						playMan.addPlayer(clientId, payloadNAME);
						outbox.add(new ToClientPacket(
								clientId,
								"CHAT", "SERVER" + DELIMITER1
								+ "Warm welcome to: " 
								+ playMan.getPlayerName(clientId) + "!")
						);
						
						connMan.getToServerQueue().add(new ToServerPacket(
								clientId,
								"CHAT" + DELIMITER1
								+ "Autotyper: Hello! I am " 
								+ playMan.getPlayerName(clientId)
								+ " just joined you in the server."));
						outbox.add(new ToClientPacket(
								clientId,
								"CHAT",
								"SERVER" + DELIMITER1
								+ "May I suggest to use: REQUESTGAME, SETTINGS, LOBBY, CHAT"
								+ " &&ingame: MOVE, QUIT."));
						
	
					} else {
						outbox.add(new ToClientPacket(clientId,
								"ERROR", "Name already taken try again:"
								+ inputLineCMD));
	
					}
					
				} catch (ArrayIndexOutOfBoundsException e) {
					
					e.printStackTrace();
					outbox.add(new ToClientPacket(clientId,
							"ERROR", "Enter name after: "
							+ inputLineCMD));
	
				}
					
				break;
				
			default:
			
				outbox.add(new ToClientPacket(clientId,
						"ERROR",
						"May I suggest to use: REQUESTGAME, SETTINGS, LOBBY, CHAT"
						+ " &&ingame: MOVE, QUIT."));
			
		} // switch NAME, default
	}
	
	/**
	 * This servlet handles all the options when the player is in lobby or in game.
	 * SETTINGS, LOBBY, CHAT
	 * 
	 * @param	toServerPacket 	package captured from a client.
	 * @param	outbox			to allow adding to this array of outbound packets
	 * @param	clientCMD		the enum containing all available commands
	 * @param	inputLineSplit	the split inputline
	 * @return	void
	 * @see 	gameservlet()
	 * 
	 */
	private void playerGeneralServlet(ToServerPacket toServerPacket, List<ToClientPacket> outbox,
			ClientCMDs clientCMD, String[] inputLineSplit) {
		int clientId = toServerPacket.getClientId();
		
		
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
							
							outbox.add(new ToClientPacket(clientId,
									"ERROR",
									"Check argument 2 :BoardSize: SETTINGS$"
									+ inputLineSplit[1] + "$<BoardSize>"));
	
						}
						
					} catch (ArrayIndexOutOfBoundsException e) {
						outbox.add(new ToClientPacket(clientId,
								"ERROR",
								"Check arg[1]:Colour: SETTINGS$<ERROR>"));
					}
					
					outbox.add(new ToClientPacket(clientId,
							"OTHER",
							"Your P1 Settings: Colour: "
							+ playMan.getColourOf(clientId)
							+ " Boardsize:" + playMan.getBoardSizeOf(clientId)));
	
					break;
				case LOBBY:
					outbox.add(new ToClientPacket(clientId,
							"OTHER",
							"Others connected to server: " + 
							playMan.getListOfAllOtherPlayers(clientId).size() 
							+ ". List of not-ingame:"));
					
					if (playMan.getListOfAllOtherPlayers(clientId).size() == 0) {
						
						outbox.add(new ToClientPacket(clientId,
								"CMDHINT",
								"REQUESTGAME$2$RANDOM"));
						outbox.add(new ToClientPacket(clientId,
								"LOBBY", ""));
						
					} else {
						
						String otherPlayersReply = playMan.getListOfAllOtherPlayers(clientId)
					            .stream()
					            .filter(playerObj -> !playerObj.getIsInGame())
					            	.map(playerObj -> playerObj.getName() + DELIMITER2)
					            .collect(Collectors.joining());
						
						outbox.add(new ToClientPacket(clientId,
								"LOBBY",
								otherPlayersReply));
						
						outbox.add(new ToClientPacket(clientId,
								"CMDHINT",
								"REQUESTGAME$2$RANDOM"));
					}
					
					break;
					
				case CHAT:
					// Chat routes a message to all players 
					// (except the sender) with a copy of the input.
					String[] payload = inputLineSplit[1].split("\\" + DELIMITER2);
					String payloadCHAT = payload[0];
					String chatSender = playMan.getPlayerName(clientId);
					
					if (playMan.getListOfAllOtherPlayers(clientId).size() == 0) {
						
						outbox.add(new ToClientPacket(clientId, "ERROR", "No players in lobby..."));
	
						
					} else {
						playMan.getListOfAllOtherPlayers(clientId).stream().forEach(
							op -> {
								outbox.add(new ToClientPacket(op.getClientId(),
										"CHAT",
										"FROM" + DELIMITER1 + chatSender
										+ DELIMITER1 + payloadCHAT));
							}
						);
						
					}
					break;
				default:
					break;
			}
			
		} catch (IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
			
			outbox.add(new ToClientPacket(clientId,
						"ERROR",								
						"May I suggest to use: REQUESTGAME, SETTINGS, LOBBY, CHAT"
						+ " &&ingame: MOVE, QUIT."));
				
			outbox.add(new ToClientPacket(clientId,
						"ERROR",
						"May I suggest to use: REQUESTGAME, SETTINGS, LOBBY, CHAT"
						+ " &&ingame: MOVE, QUIT."));
		}
		
	}
	
	/**
	 * This servlet handles all the options when the player is in game ONLY.
	 * SETTINGS, LOBBY, CHAT
	 * 
	 * @param	toServerPacket 	package captured from a client.
	 * @param	outbox			to allow adding to this array of outbound packets
	 * @param	clientCMD		the enum containing all available commands
	 * @param	inputLineSplit	the split inputline
	 * @return	void
	 * @see 	gameservlet()
	 * 
	 */

	private void playerIsInGameServlet(ToServerPacket toServerPacket, List<ToClientPacket> outbox,
			ClientCMDs clientCMD, String[] inputLineSplit) {
		
		int clientId = toServerPacket.getClientId();
		try {
			switch (clientCMD) {
				case MOVE:
					
					int moveDataRow = 0;
					int moveDataCol = 0;
					
					try {
						
						// To decrypt: MOVE%1_2
						String[] moveData = inputLineSplit[1].split(DELIMITER2);
						
						if (moveData[0].equals("PASS")) {
							
							gameMan.passFor(clientId);
							
						} else {
						
							moveDataRow = Integer.parseInt(moveData[0]);
							moveDataCol = Integer.parseInt(moveData[1]);
							
							if (moveDataRow < 0
									|| 
									moveDataRow > 
									gameMan.getGameObjForClient(clientId).getBoardSize() - 1
									|| 
									moveDataCol < 0 
									|| 
									moveDataRow >
									gameMan.getGameObjForClient(clientId).getBoardSize() - 1) {
								
								// Here already apply the rule to correct
								// for starting at array integer 0 instead of 1 as in GUI/TUI.
								// Send message
								gameMan.getGameObjForClient(clientId).messageClientId(clientId,
										"INVALIDMOVE",
										"MOVE NOT ALLOWED, OUT OF BOUNDS");
								
							} else {
								
								gameMan.tryMoveFor(clientId, moveDataRow, moveDataCol);
								
							}
						}
	
					} catch (NullPointerException |
							ArrayIndexOutOfBoundsException |
							NumberFormatException e) {
						
						e.printStackTrace();
						outbox.add(new ToClientPacket(clientId,
								"INVALIDMOVE",
								"Please use the format: MOVE$row_col"));
					}
					
					break;
				case QUIT:
					outbox.add(new ToClientPacket(clientId,
							"OTHER",
							"Quitting..."));
					
					gameMan.quit2PGameFor(clientId);
					break;
				default:
					break;
			}
			
		} catch (IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
			
			outbox.add(new ToClientPacket(clientId,
					"ERROR",								
					"May I suggest to use: REQUESTGAME, SETTINGS, LOBBY, CHAT"
					+ " &&ingame: MOVE, QUIT."));
			
			outbox.add(new ToClientPacket(clientId,
					"ERROR",
					"May I suggest to use: REQUESTGAME, SETTINGS, LOBBY, CHAT"
					+ " &&ingame: MOVE, QUIT."));
		}
	}

	/**
	 * This servlet handles all the options when the player is NOT in game
	 * REQUESTGAME.
	 * 
	 * @param	toServerPacket 	package captured from a client.
	 * @param	outbox			to allow adding to this array of outbound packets
	 * @param	clientCMD		the enum containing all available commands
	 * @param	inputLineSplit	the split inputline
	 * @return	void
	 * @see 	gameservlet()
	 * 
	 */

	private void playerNotInGameServlet(ToServerPacket toServerPacket,
			List<ToClientPacket> outbox,
			ClientCMDs clientCMD,
			String[] inputLineSplit) {
		try {
			int clientId = toServerPacket.getClientId();
			
			switch (clientCMD) {
				case REQUESTGAME:
					// Example what to expect: REQUESTGAME$<int players>$<string against>
					try {
						// Challenge
						//int amountOfPlayers= Integer.parseInt(inputLineSplit[1]);
						//String playingAgainst= inputLineSplit[2];
						
						gameMan.addToRequestQueue(toServerPacket);
						
						// The queue will be processed by another step "the game manager"
						gameMan.processRequestQueue();
						
					} catch (IllegalArgumentException |
							ArrayIndexOutOfBoundsException e) {
						outbox.add(new ToClientPacket(clientId,
								"ERROR",
								"RequestGame error"));
					}				
					break;
				default:
					break;
			}
			
		} catch (IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
			outbox.add(new ToClientPacket(toServerPacket.getClientId(),
					"ERROR",
					"RequestGame error."));
		}

	}

	/**
	 * Delimiter 2 handles the sign used to replace any spaces users use
	 * Within delimiter 1.
	 * 
	 * Example: MOVE<DELIMITER1>ROW<DELIMITER2>COLLUMN
	 * 
	 */
	
	public static String getDELIMITER2() {
		return DELIMITER2;
	}

	/**
	 * Delimiter 1 handles the sign used in network communication to 
	 * filter command from payload.
	 * 
	 * 
	 * Example: COMMAND<DELIMITER1>PAYLOAD<DELIMITER1>COMMAND<DELIMITER1>PAYLOAD.
	 * 
	 */
	public static String getDELIMITER1() {
		return DELIMITER1;
	}


	
}
