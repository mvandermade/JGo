package client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import board.Board;
import board.BoardKoRuleViolatedE;
import board.Stone;
import clientView.ClientOutToServerPacket;
import clientView.ClientTextInputPacket;
import clientView.ServerInToClientPacket;
import gui.GoGUIIntegrator;

public class Client {
	
	// TUI
	final Queue<ClientTextInputPacket> clientTextInputQueue =
			new ConcurrentLinkedQueue<ClientTextInputPacket>();
	
	// Outbox TCP
	final Queue<ClientOutToServerPacket> clientOutToServerQueue =
			new ConcurrentLinkedQueue<ClientOutToServerPacket>();
	
	// Inbox TCP
	final Queue<ServerInToClientPacket> serverInToClientQueue =
			new ConcurrentLinkedQueue<ServerInToClientPacket>();
	
	private Socket skt = null;
	
	private BufferedWriter bufferedWriterToServer = null;

	// Used to control the GUI and Board
	private Boolean inGame = false; 
	private Board board;
	private String startColourP1 = null;
	
	private GoGUIIntegrator gogui = null;
	
	private String delimiter1 = "$";
	private String delimiter2 = "_";
	
	private int pollQueueTime;
	
	// For the game initialisation
	private String playerName = null;
	//Server details
	private String serverHostname = null;
	private String serverPort = null;
	
	
	// For determining the colors
	private int playerNoSelf = 0;
	private int playerNoOther = 0;
	private int playerNoSelfScore = 0;
	private int playerNoOtherScore = 0;

	private String startColourPlayer;
	
    /**
     * The client application is designed to connect to a server that
     * Uses the communication protocol 5 from the nedap university 3.
     * 
     * It acts as a 'smart' terminal, meaning that commands can be 
     * rerouted to for instance a graphical user interface.
     * Also the client filters messages the server sends.
     * 
     * The internal messaging service has similarities with the server
     * Only in case of the client all methods are shared in one class.
     * This was mainly done due to the easier exchange of fields.
     * 
     *
     * 
     */
	public Client() {
		
		System.out.println("May I suggest to use: REQUESTGAME, SETTINGS, LOBBY, CHAT" 
				+ " &&ingame: MOVE, QUIT.");
		System.out.println("DELIMITERS: COMMAND<DELIMITER1>PAYLOAD");
		System.out.println("PAYLOAD IS DELIMITED USING <DELIMITER2>");
		System.out.println("EXAMPLE: MOVE" + delimiter1 + "1" + delimiter2 + "2");
		System.out.println("SEE NU3.0 v5.0 protocol for details.");
		System.out.println("type HELP for info");
		System.out.println("Hi! Please state your player name:");
		
		// Poll for the name of the player
		while (null == playerName) {
			try {
				playerName = (new BufferedReader(new InputStreamReader(System.in))).readLine();
			} catch (IOException e) {
				System.out.println("Need a name to continue");
				e.printStackTrace();
			}
			
		}
		
		
		System.out.println("Client: got:, " 
				+ playerName
				+ " as input, please enter the IP or Hostname of a server: (none = localhost)");
		
		try {
			serverHostname = (new BufferedReader(new InputStreamReader(System.in))).readLine();
		} catch (IOException e) {
			System.out.println("--> Autofill: localhost");
			serverHostname = "localhost";
		}
		
		if (serverHostname.equals("")) {
			System.out.println("-> Autofill: localhost");
			serverHostname = "localhost";
		}
		
		System.out.println("Client: got:, " 
				+ serverHostname
				+ " as input, please enter the port (enter = default NU3.0: 5647):");
		
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
		
		// Default hello message for nedap university 3.0 protocol v5
		clientOutToServerQueue.add(new ClientOutToServerPacket(
				"NAME"
				+ delimiter1 
				+ playerName
				+ delimiter1
				+ "VERSION"
				+ delimiter1
				+ "6"
				+ delimiter1
				+ "EXTENSIONS"
				+ delimiter1
				+ "1" + delimiter1 + "1" + delimiter1 + "1" + delimiter1 + "1" + delimiter1
				+ "1" + delimiter1 + "1" + delimiter1 + "1" + delimiter1
				));
		
		
		// 20ms mainloop delay
		this.pollQueueTime = 20;
		
		try {
			skt = new Socket(serverHostname, Integer.parseInt(serverPort));
			bufferedWriterToServer = new BufferedWriter(
					new OutputStreamWriter(
							skt.getOutputStream()
							)
					);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("Connected to: " + serverHostname + ":" + serverPort);
		System.out.println("Need to send stuff here... version and stuff");
		
		// Use a part of the server thread
		(new Thread() {
			public void run() {
			// do something 
				handleClientInbox();
			}
		}).start(); // Boot the thread
		
		// Handle console 
		(new Thread() {
			public void run() {
			// do something 
				handleTUI(new BufferedReader(new InputStreamReader(System.in)));
			}
		}).start(); // Boot the thread
		
		
		// Start the running part
		runClientLoop();
		
	}
	
    /**
     * In stages this loop will continously poll from again the network socket
     * connected to the server, as well as the System.in reader.
     * The GUI also puts event on this polling loop.
     * 
     * After polling a package with a command for the client the commands
     * are parsed in order of creating via sorting.
     * 
     * Then any responses created during one loop iteration are sent 
     * back to the server.
     * 
     * @param	
     * @return	void
     */
	
	private void runClientLoop() {
		while (true) {
			try {
				Thread.sleep(pollQueueTime);
			} catch (InterruptedException e) {
				// ignore
			}
			
			// 1. Since the 1 'inbox' thread is running asynchrone, the poll method is used.
			// Timestamps are then used to sort
			
			// Writes to serverInToClientQueue
			clientPullAndProcessInbox();
			
			//2 See the input scanner for new messages and process them
			clientPullAndProcessTUI();
			
			//3 Check the GUI (if on)
			if (null != gogui) {
				gogui.getGuiClickResultToArray().forEach((guiClick) -> {
					
					// Pretend as if the gui types an inputline and handle it.
					System.out.println("■");
					System.out.println(guiClick.getInputLine());
					clientTUIResponder(new ClientTextInputPacket("MOVE"
							+ delimiter1
							+ guiClick.getInputLine()));
				});
			}
			
			//4. Packets are sent out async.
			flushClientOutbox();
			
		} // end while true client loop
		
	}

    /**
     * The network inbox here is pulled towards the main thread from the
     * socket handing object.
     * The queue is polled, sorted and stored to the main thread variables.
     * 
     * @param	
     * @return	void
     */
	private void clientPullAndProcessInbox() {
		List<ServerInToClientPacket> localPolledQueue = new ArrayList<ServerInToClientPacket>();
		Boolean done = false;
		
		while (!done) {
			ServerInToClientPacket polledObject = serverInToClientQueue.poll();
			// null is the native response for 'no queue items left'
			if (polledObject != null) {
				localPolledQueue.add(polledObject);
			} else {
				done = true;
			}
		}
		
		// Synchronize using timestamping. So output can be done in order of array.
		List<ServerInToClientPacket> servletQueue = localPolledQueue.stream()
				.sorted((f1, f2) -> Long.compare(f1.getStartTime(), f2.getStartTime())).
                collect(Collectors.toList());
		
		// Pass all to clientResponderServlet
		servletQueue.forEach((c) -> {
			//System.out.println("GOT TCP+++++++"); 
			clientResponderServlet(c); 
			//System.out.println("END TCP-------");
			
		});
		
	}
	
	private void clientPullAndProcessTUI() {
		// Queue<ClientTextInputPacket> clientTextInputPacket
		List<ClientTextInputPacket> localPolledQueue = new ArrayList<ClientTextInputPacket>();
		Boolean done = false;
		
		while (!done) {
			ClientTextInputPacket polledObject = clientTextInputQueue.poll();
			// null is the native response for 'no queue items left'
			if (polledObject != null) {
				localPolledQueue.add(polledObject);
			} else {
				done = true;
			}
		}
		
		// Synchronize using timestamping. So output can be done in order of array.
		List<ClientTextInputPacket> textQueue = localPolledQueue.stream()
				.sorted((f1, f2) -> Long.compare(f1.getStartTime(), f2.getStartTime())).
                collect(Collectors.toList());
		
		// Pass all to clientResponderServlet
		textQueue.forEach((c) -> {
			//System.out.println("GOT TEXT..."); 
			//System.out.println(c.getInputLine());
			clientTUIResponder(c); 
			//System.out.println("END TEXT...");
			
		});
		
	}
	

    /**
     * Enum used to decode server message commands.
     * In this case they are split up in an enum allowed always:
     * and one only allowed during a game
     */
	
	// These commands can be sent out to the server
	
	private enum ServerCMDs {
		
		START,
		CHAT,
		LOBBY,
		HELP
	}
	
    /**
     * Enum used to decode server message commands.
     * In this case they are split up in an enum allowed always:
     * and one only allowed during a game
     */
	// These commands can be sent out to the server only ingame
	
	private enum ServerCMDsInGame {
		
		MOVE,
		TURN,
		ENDGAME,
		ERROR
		
	}
	
    /**
     * The clientresponder servlet takes care of all logics that need to.
     * occur to purposefully respond to server messages.
     * 
     * The servlet can also interact with the external package GoGui if desired.
     * 
     * The ingame state of the user is stored in the main object field, as well
     * as the desired settings for a game.
     */

	private void clientResponderServlet(ServerInToClientPacket toClientPacket) {
		
		// Intelligence happens here
		// Talk back to Command line or GUI
		
		// Parseint seems to be a static function, so beware. Therefore all
		// Reads are redone after a ParseInt.
		
		// This part can only be accessed if ClientPlayerName exists
		String inputLineCMD = toClientPacket.getInputLine();
	
		String[] checkInputIsClientCompliant = inputLineCMD.split("\\" + delimiter1);
		
		
		Boolean printServerDebug = false;
		
		try {
			ServerCMDs.valueOf(checkInputIsClientCompliant[0]);
			printServerDebug = true;
		} catch (IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
			// Not interesting to report
			
		}
		
		try {
			ServerCMDsInGame.valueOf(checkInputIsClientCompliant[0]);
			printServerDebug = true;
		} catch (IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
			// Not interesting to report
			
		}
		
		if (printServerDebug) {
			
			System.out.println(">Server> " + inputLineCMD);
			printServerDebug = false;
		}
		
		try {
			String[] inputLineSplit = inputLineCMD.split("\\" + delimiter1);
			
			ServerCMDs clientCMDEnumVal = ServerCMDs.valueOf(inputLineSplit[0]);
			
			switch (clientCMDEnumVal) {
				case START:
					
					// The START Command is used twice in a row.
					// When only START$<playerNo> is seen, send the settings back to the server.
					// So START 2
					// Then respond with SETTINGS$BLACK$19
					//
					// BUT! When more are seen:
					// START 2 BLACK 19 jan piet
					
					// You can change this anytime, Before or after invoking RequestGame.
					// Settings will be leading only if P1 position.
					
					// First try to read the [2] argument
					
					try {
						startColourP1 = inputLineSplit[2];
						try {
							//inputLineSplit[2];
							int boardSize = Integer.parseInt(inputLineSplit[3]);
							setGameStateTrue(startColourP1, boardSize);
							
						} catch (ArrayIndexOutOfBoundsException e) {
							System.out.println("Client: Could not read boardSize");
							
						}
						
					} catch (ArrayIndexOutOfBoundsException e) {
						
						// 2 is ignored here.
						System.out.println("Client: Could not read startColour..."
								+ "... sending settings");
						clientTextInputQueue.add(new ClientTextInputPacket(
								"SETTINGS$BLACK$19"));
						// Now expecting a server packet that looks like this:
						// START 2 BLACK 19 jan piet
					}
					
	
					break;
				case HELP:
					System.out.println("May I suggest to use: REQUESTGAME, SETTINGS, LOBBY, CHAT"
							+ " &&ingame: MOVE, QUIT.");
					break;
				default:
					break;
			} // endof switch clientCMD
				
		} catch (IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
				
			// Dont report it is printed anyway.
				
		}
			
		try {
			// 
			String[] inputLineSplit = inputLineCMD.split("\\" + delimiter1);
			if (true == inGame) {
				ServerCMDsInGame clientTurnCMDEnumVal = ServerCMDsInGame.valueOf(inputLineSplit[0]);
				
				switch (clientTurnCMDEnumVal) {				
					case TURN:
						// You can change this anytime, Before or after invoking RequestGame.
						// Settings will be leading only if P1 position.
						
						
						try {
							
							// Determine the playerNo
							if (inputLineSplit[2].equals("FIRST")) {
								
								if (playerName.equals(inputLineSplit[1])) {
									
									System.out.println("You may go first!");
									playerNoSelf = 1;
									playerNoOther = 2;
									
									this.startColourPlayer = startColourP1;
									
									gogui.changeGuiTitle("TURN " + playerName 
											+ " " + startColourPlayer);
									
								} else {
									
									System.out.println("The other may begin, but I hope you win!");
									playerNoSelf = 2;
									playerNoOther = 1;
									
									this.startColourPlayer = "WHITE";
									
									gogui.changeGuiTitle(playerName + " " + startColourPlayer);
									
								}
								
								// Not first, but it can be either a move accepted from you
								// TURN$OPPONENT$1_2$YOU
								// TURN$YOU$2_2$OPPONENT
								// TURN$<TAKE>$<MOVE>$<BYLAST>
							} else if (inputLineSplit[2].equals("PASS")) {
								
								if (playerName.equals(inputLineSplit[1])) {
									
									System.out.println("Opponent passed");
									
									gogui.changeGuiTitle("TURN " 
											+ playerName + " " + startColourPlayer);
									
								} else {
									
									gogui.changeGuiTitle(playerName + " " + startColourPlayer);
									
								}
								
							} else {
								
	
								
								String takeNext = inputLineSplit[3];
								String move = inputLineSplit[2];
								String byPlayer = inputLineSplit[1];
								
								String[] moveSplit = move.split("\\" + delimiter2);
								
								// Correction
								String rowstr = moveSplit[0];
								int row = Integer.parseInt(rowstr) - 1;
								
								try {
									
									//inputLineSplit[2];
									String colstr = moveSplit[1];
									int col = Integer.parseInt(colstr) - 1;
									
									// Track move
									
									if (byPlayer.equals(playerName)) {
										
										trackServerMove(playerNoSelf, row, col);
	
									} else {
										
										trackServerMove(playerNoOther, row, col);
										
									}
									
									if (playerNoSelf == 1) {
										playerNoSelfScore = board.getScoreP1();
										playerNoOtherScore = board.getScoreP2();
									} else if (playerNoSelf == 2) {
										playerNoSelfScore = board.getScoreP2();
										playerNoOtherScore = board.getScoreP1();
									}
									
	
									
									// Inform user of turn by server
									
									if (takeNext.equals(playerName)) {
										
										System.out.println("Hey, "
											+ playerName + "(P" + playerNoSelf + ") it's your turn!"
											+ " Your score: " + playerNoSelfScore + " Opponent: "
											+ playerNoOtherScore);
										
										
										gogui.changeGuiTitle("TURN "
												+ playerName + " " + startColourPlayer 
												+ " Your score: " + playerNoSelfScore
												+ " Opponent: " + playerNoOtherScore);
										
									} else {
										
										System.out.println("Turn of: " + takeNext);
										gogui.changeGuiTitle(playerName
												+ " " + startColourPlayer 
												+ " Your score: " + playerNoSelfScore 
												+ " Opponent: " + playerNoOtherScore);
										
									}
									
								} catch (ArrayIndexOutOfBoundsException e) {
									System.out.println("Fault at TURN");
									e.printStackTrace();
								}
								
							}
							
							
						} catch (ArrayIndexOutOfBoundsException e) {
							
							
						}
						
		
						break;
					default:
						break;
				} // endof switch
				
			}
			
			
		} catch (IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
			
			// Dont report it is printed anyway.
			
		}
		
	}
	
    /**
     * Tracking the response of the server, with the GUI, 
     * but also by moving on an own internal board.
     * This board is shared with the server, and thus also checking rules !
     * @param moveForPlayerNo.	specify the player number to move for.
     * @param row				row number to put stone
     * @param col				collumn number to put stone
     */
	private void trackServerMove(int moveForPlayerNo, int row, int col) {
		
		if (board.isMoveValid(moveForPlayerNo, row, col)) {
			
			try {
				List<Stone> toRemoveStonesGui = board.putStoneForPlayer(moveForPlayerNo, row, col);
				// false = 1, true = 2
				if (moveForPlayerNo == 1) {
					gogui.addStoneRC(row, col, false);
				} else if (moveForPlayerNo == 2) {
					gogui.addStoneRC(row, col, true);
				}
				
				toRemoveStonesGui.forEach((stone) -> {
					gogui.removeStoneRC(stone.getRow(), stone.getCol());
				});
				
			} catch (BoardKoRuleViolatedE e) {
				
				System.out.println("Ko rule violation ! Try again.");
			}
			
			// The server announces that the other player has moved
			if (playerNoOther == moveForPlayerNo) {
				buddyAI();
			}
			
		}
		
	}

	/**
	 * An AI which places a marker on the board and prints out text for you to copy.
	 * You can trigger it to post for you if you flip the settings switch
	 */
	public void buddyAI() {
		
		gogui.removeHintIdicator();
		
		int aiX = ThreadLocalRandom.current().nextInt(0, board.getBoardSize());
		int aiY = ThreadLocalRandom.current().nextInt(0, board.getBoardSize());
		
		int aiCol = aiX + 1;
		int aiRow = aiY + 1;
		// Right now totally random advice.
		// In future use functionality of Board to decide based on chain length/shape etc.
		// Create some sort of 'fitness function'.
		System.out.println("<buddyAI> HINT:");
		String buddyAIout = "MOVE$" + aiCol + "_" + aiRow;
		System.out.println(buddyAIout);
		if (playerName.equals("AIAI") || playerName.equals("AIAI2")) {
			clientTextInputQueue.add(new ClientTextInputPacket(
					buddyAIout));
			System.out.println("buddy moves for you!");
		}
		
		gogui.addHintIndicator(aiX, aiY);
		
	}
    /**
     * Set the game state of the client to True.
     * Also launches a GUI to communicate with.
     * @param startColourP1input	Colour of interest (string)
     * @param boardSize				Size of the square board (boardSize x boardSize
     */
	private void setGameStateTrue(String startColourP1input, int boardSize) {
		inGame = true;
		
		this.startColourP1 = startColourP1input;
		
		// Internal tracking
		board = new board.Board(boardSize);
		
		gogui = new GoGUIIntegrator(true, true, boardSize);
		
        gogui.startGUI();
        gogui.setBoardSize(boardSize);
        gogui.changeGuiTitle(playerName);

	}
    /**
     * Grabs command line input and sends it directly to the server.
     * @param clientTextInputPacket
     */
	private void clientTUIResponder(ClientTextInputPacket c) {
		
		// Filtering of commands
		// Things like that
		
		// Then afterwards post it
		
		// Intelligence happens here
		
		// Put into sender queue
		clientOutToServerQueue.add(new ClientOutToServerPacket(c.getInputLine()));
		// Second ✓
		System.out.print("✓");
		
	}

    /**
     * Send all the enqueued packets to the server.
     * 
     */
	
	private void flushClientOutbox() {
		
		// Queue<ClientTextInputPacket> clientTextInputPacket
		List<ClientOutToServerPacket> localPolledQueue = new ArrayList<ClientOutToServerPacket>();
		Boolean done = false;
		
		while (!done) {
			ClientOutToServerPacket polledObject = clientOutToServerQueue.poll();
			// null is the native response for 'no queue items left'
			if (polledObject != null) {
				localPolledQueue.add(polledObject);
			} else {
				done = true;
			}
		}
		
		// Synchronize using timestamping. So output can be done in order of array.
		List<ClientOutToServerPacket> textQueue = localPolledQueue.stream()
				.sorted((f1, f2) -> Long.compare(f1.getStartTime(), f2.getStartTime())).
                collect(Collectors.toList());
		
		// Pass all to clientResponderServlet
		
		if (textQueue.size() > 0) {
			textQueue.forEach((c) -> {
				//System.out.println("SENDING OUT..."); 
				// Write line
				try {
					bufferedWriterToServer.write(c.getInputLine());
					// Protocol
					bufferedWriterToServer.newLine();
					// Send
					bufferedWriterToServer.flush();
					
					
					
				} catch (IOException e) {
					// Do not report
				}
				//System.out.println("END OUT.......");
				
			});
			
			System.out.println("↗");
		}
		

		
	}

    /**
     * Handler for the attached socket with the server.
     * All caught lines in UTF-8 that are \n delimited are enqueued.
     */
	
	// Method to receive messages
	private void handleClientInbox() {
		
		// Respond to the server input
		// Auto closing
		try (
                BufferedReader in = new BufferedReader(new InputStreamReader(skt.getInputStream()));
            ) {
            String inputLineUTF8;
            String inputLine = null; //UTF16
            while ((inputLineUTF8 = in.readLine()) != null) {
            	
            	try {
            	    // Convert from Unicode to UTF-8
            	    byte[] utf8 = inputLineUTF8.getBytes("UTF-8");

            	    // Convert from UTF-8 to Unicode
            	    inputLine = new String(utf8, "UTF-8");
            	} catch (UnsupportedEncodingException e) {
            	}
            	
            	// Add packet to the queue if a \n is seen.
            	
            	serverInToClientQueue.add(new ServerInToClientPacket(inputLine));
            	
            }
            
        } catch (IOException e) {
        	e.printStackTrace();
        }
		
	}
	
    /**
     * Handler for the attached system.in for console inputs.
     */
	private void handleTUI(BufferedReader bufferedReaderTextInput) {
		while (true) {
			try {
				// Each line is seen as a command, and put into the queue
				
				clientTextInputQueue.add(new ClientTextInputPacket(
						bufferedReaderTextInput.readLine()));
				// First ✓
				System.out.print("✓");
				
			} catch (IOException e) {
				// ignore
				//e.printStackTrace();
			}
		}
		
		
	}
	
	

}
