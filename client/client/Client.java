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
import java.util.stream.Collectors;

import board.Board;
import board.BoardKoRuleViolated;
import board.Stone;
import gui.GoGUIIntegrator;

public class Client {
	
	
	// TUI
	final Queue<ClientTextInputPacket> clientTextInputQueue = new ConcurrentLinkedQueue<ClientTextInputPacket>();
	
	// Outbox TCP
	final Queue<ClientOutToServerPacket> clientOutToServerQueue = new ConcurrentLinkedQueue<ClientOutToServerPacket>();
	// Inbox TCP
	final Queue<ServerInToClientPacket> serverInToClientQueue = new ConcurrentLinkedQueue<ServerInToClientPacket>();
	
	private Socket skt = null;
	
	private BufferedWriter bufferedWriterToServer = null;

	// Used to control the GUI and Board
	private Boolean inGame = false; 
	private Board board;
	private String startColourP1 = null;
	
	private GoGUIIntegrator gogui = null;
	
	private String DELIMITER1 = "$";
	private String DELIMITER2 = "_";
	
	private int pollQueueTime;
	
	// For the game initialisation
	private String playerName = null;
	
	// For determining the colors
	private int playerNoSelf = 0;
	private int playerNoOther = 0;
	private int playerNoSelfScore = 0;
	private int playerNoOtherScore = 0;

	private String startColourPlayer;
	

	public Client(String servername, int port) {
		// TODO Auto-generated constructor stub
		
		System.out.println("Hi! Please state your player name:");
		
		// Poll for the name of the player
		while(null == playerName) {
			try {
				playerName = (new BufferedReader(new InputStreamReader(System.in))).readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("Need a name to continue");
				e.printStackTrace();
			}
			System.out.println("Client: got:, "+playerName+" as input, please enter the IP or Hostname of a server:");
		}
		
		clientOutToServerQueue.add(new ClientOutToServerPacket("NAME"+DELIMITER1+playerName));
		
		clientOutToServerQueue.add(new ClientOutToServerPacket("REQUESTGAME"));
		// Repeate the playerName bufferedreader concept for IP and port (parseToInt)
		
		
		
		// 200ms mainloop delay
		this.pollQueueTime = 200;
		
		try {
			skt = new Socket(servername, port);
			bufferedWriterToServer = new BufferedWriter( new OutputStreamWriter( skt.getOutputStream()) );
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Connected to: "+servername+":"+port);
		System.out.println("Need to send stuff here... version and stuff");
		
		// Use a part of the server thread
		(new Thread() {
			public void run() {
			// do something 
				HandleClientInbox();
			}
		}).start(); // Boot the thread
		
		// Handle console 
		(new Thread() {
			public void run() {
			// do something 
				HandleTUI(new BufferedReader(new InputStreamReader(System.in)));
			}
		}).start(); // Boot the thread
		
		
		// Start the running part
		runClientLoop();
		
	}
	
	private void runClientLoop() {
		// TODO Auto-generated method stub
		while (true) {
			try {
				Thread.sleep(pollQueueTime);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			// 1. Since the 1 'inbox' thread is running asynchrone, the poll method is used.
			// Timestamps are then used to sort
			
			// Writes to serverInToClientQueue
			clientPullAndProcessInbox();
			
			//2 See the input scanner for new messages and process them
			clientPullAndProcessTUI();
			
			//3 Check the GUI (if on)
			if (null != gogui) {
				gogui.getGuiClickResultToArray().forEach((guiClick)->{
					
					// Pretend as if the gui types an inputline and handle it.
					System.out.println("■");
					System.out.println(guiClick.getInputLine());
					clientTUIResponder(new ClientTextInputPacket("MOVE"+DELIMITER1+guiClick.getInputLine()));
				});
			}
			
			//4. Packets are sent out async.
			FlushClientOutbox();
			
		} // end while true client loop
		
	}

	private void clientPullAndProcessInbox() {
		// TODO Auto-generated method stub
		List<ServerInToClientPacket> localPolledQueue = new ArrayList<ServerInToClientPacket>();
		Boolean done = false;
		
		while(!done) {
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
		servletQueue.forEach((c)->{
			//System.out.println("GOT TCP+++++++"); 
			clientResponderServlet(c); 
			//System.out.println("END TCP-------");
			
		});
		
	}
	
	private void clientPullAndProcessTUI() {
		// TODO Auto-generated method stub
		// Queue<ClientTextInputPacket> clientTextInputPacket
		List<ClientTextInputPacket> localPolledQueue = new ArrayList<ClientTextInputPacket>();
		Boolean done = false;
		
		while(!done) {
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
		textQueue.forEach((c)->{
			//System.out.println("GOT TEXT..."); 
			//System.out.println(c.getInputLine());
			clientTUIResponder(c); 
			//System.out.println("END TEXT...");
			
		});
		
	}
	
	// These commands can be sent out to the server
	
	private enum ServerCMDs {
		
		START,
		CHAT,
		LOBBY
	}
	
	// These commands can be sent out to the server only ingame
	
	private enum ServerCMDsInGame {
		
		MOVE,
		TURN,
		ENDGAME,
		ERROR
		
	}
	

	private void clientResponderServlet(ServerInToClientPacket cRx) {
		// TODO Auto-generated method stub
		
		// Intelligence happens here
		// Talk back to Command line or GUI
		
		// Parseint seems to be a static function, so beware. Therefore all
		// Reads are redone after a ParseInt.
		
		// This part can only be accessed if ClientPlayerName exists
		String inputLineCMD = cRx.getInputLine();
		
		/////
	
		String[] checkInputIsClientCompliant = inputLineCMD.split("\\"+DELIMITER1);
		
		
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
		
		if(printServerDebug) {
			
			System.out.println("Server(filtered) said: "+inputLineCMD);
			printServerDebug = false;
		}
		
		try {
			String[] inputLineSplit = inputLineCMD.split("\\"+DELIMITER1);
			
			ServerCMDs clientCMDEnumVal = ServerCMDs.valueOf(inputLineSplit[0]);
			
			switch (clientCMDEnumVal) {
			case START:
				// You can change this anytime, Before or after invoking RequestGame.
				// Settings will be leading only if P1 position.
				try {
					startColourP1 = inputLineSplit[1];
					try {
						//inputLineSplit[2];
						int boardSize = Integer.parseInt(inputLineSplit[2]);
						setGameStateTrue(startColourP1, boardSize);			
					} catch (ArrayIndexOutOfBoundsException e) {
						System.out.println("Could not read boardSize...");
						
					}
					
				} catch (ArrayIndexOutOfBoundsException e) {
					
					System.out.println("Could not read startColour...");
				}
				

				break;
			default:
				break;
			} // endof switch clientCMD
			
		} catch (IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
			
			// Dont report it is printed anyway.
			
		}
		
		try {
			// 
			String[] inputLineSplit = inputLineCMD.split("\\"+DELIMITER1);
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
								
								gogui.changeGuiTitle("TURN "+playerName+" "+startColourPlayer);
								
							} else {
								
								System.out.println("The other may begin, but I hope you win!");
								playerNoSelf = 2;
								playerNoOther = 1;
								
								this.startColourPlayer = "WHITE";
								
								gogui.changeGuiTitle(playerName+" "+startColourPlayer);
								
							}
							
							// Not first, but it can be either a move accepted from you
							// TURN$OPPONENT$1_2$YOU
							// TURN$YOU$2_2$OPPONENT
							// TURN$<TAKE>$<MOVE>$<BYLAST>
						} else if(inputLineSplit[2].equals("PASS")) {
							
							if (playerName.equals(inputLineSplit[1])) {
								
								System.out.println("Opponent passed");
								
								gogui.changeGuiTitle("TURN "+playerName + " "+startColourPlayer);
								
							} else {
								
								gogui.changeGuiTitle(playerName+ " "+startColourPlayer);
								
							}
							
						} else {
							

							
							String takeNext = inputLineSplit[1];
							String move = inputLineSplit[2];
							String byPlayer = inputLineSplit[3];
							
							String[] moveSplit = move.split("\\"+DELIMITER2);
							
							// Correction
							String rowstr = moveSplit[0];
							int row = Integer.parseInt(rowstr) -1;
							
							try {
								
								//inputLineSplit[2];
								String colstr = moveSplit[1];
								int col = Integer.parseInt(colstr) -1;
								
								// Track move
								
								if(byPlayer.equals(playerName)) {
									
									trackServerMove(playerNoSelf, row, col);

								} else {
									
									trackServerMove(playerNoOther, row, col);
									
								}
								
								if(playerNoSelf == 1) {
									playerNoSelfScore = board.getScoreP1();
									playerNoOtherScore = board.getScoreP2();
								} else if (playerNoSelf == 2) {
									playerNoSelfScore = board.getScoreP2();
									playerNoOtherScore = board.getScoreP1();
								}
								

								
								// Inform user of turn by server
								
								if(takeNext.equals(playerName)) {
									
									System.out.println("Hey, "+playerName+"(P"+playerNoSelf+") it's your turn!"+" Your score: "+playerNoSelfScore+" Opponent: "+playerNoOtherScore);
									
									
									gogui.changeGuiTitle("TURN "+playerName+" "+startColourPlayer+" Your score: "+playerNoSelfScore+" Opponent: "+playerNoOtherScore);
									
								} else {
									
									System.out.println("Turn of: "+takeNext);
									gogui.changeGuiTitle(playerName+" "+startColourPlayer+" Your score: "+playerNoSelfScore+" Opponent: "+playerNoOtherScore);
									
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
	
	private void trackServerMove(int moveForPlayerNo, int row, int col) {
		
		if(board.isMoveValid(moveForPlayerNo, row, col)) {
			
			try {
				// TODO Auto-generated method stub
				List<Stone> toRemoveStonesGui=board.putStoneForPlayer(moveForPlayerNo, row, col);
				// false = 1, true = 2
				if (moveForPlayerNo == 1) {
					gogui.addStoneRC(row, col, false);
				} else if (moveForPlayerNo == 2) {
					gogui.addStoneRC(row, col, true);
				}
				
				toRemoveStonesGui.forEach((stone)-> {
					gogui.removeStoneRC(stone.getRow(), stone.getCol());
				});
				
			} catch (BoardKoRuleViolated e) {
				
				System.out.println("Ko rule violation ! Try again.");
			}


			
		}
		
		
	}

	private void setGameStateTrue(String startColourP1, int boardSize) {
		// TODO Auto-generated method stub		
		inGame = true;
		
		this.startColourP1 = startColourP1;
		
		// Internal tracking
		board = new board.Board(boardSize);
		
		gogui = new GoGUIIntegrator(true, true, boardSize);
		
        gogui.startGUI();
        gogui.setBoardSize(boardSize);
        gogui.changeGuiTitle(playerName);

	}

	private void clientTUIResponder(ClientTextInputPacket c) {
		// TODO Auto-generated method stub
		
		// Filtering of commands
		// Things like that
		
		// Then afterwards post it
		
		// Intelligence happens here
		
		// Put into sender queue
		clientOutToServerQueue.add(new ClientOutToServerPacket(c.getInputLine()));
		// Second ✓
		System.out.print ("✓");
		
	}

	private void FlushClientOutbox() {
		// TODO Auto-generated method stub
		
		// Queue<ClientTextInputPacket> clientTextInputPacket
		List<ClientOutToServerPacket> localPolledQueue = new ArrayList<ClientOutToServerPacket>();
		Boolean done = false;
		
		while(!done) {
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
			textQueue.forEach((c)->{
				//System.out.println("SENDING OUT..."); 
				// Write line
				try {
					bufferedWriterToServer.write(c.getInputLine());
					// Protocol
					bufferedWriterToServer.newLine();
					// Send
					bufferedWriterToServer.flush();
					
					
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//System.out.println("END OUT.......");
				
			});
			
			System.out.println("↗");
		}
		

		
	}

	// Method to receive messages
	private void HandleClientInbox() {
		
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
	
	private void HandleTUI(BufferedReader bufferedReaderTextInput) {
		// TODO Auto-generated method stub
		while (true) {
			try {
				// Each line is seen as a command, and put into the queue
				
				clientTextInputQueue.add(new ClientTextInputPacket(bufferedReaderTextInput.readLine()));
				// First ✓
				System.out.print("✓");
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
	}
	
	

}
