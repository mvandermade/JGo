package serverModel;

import java.util.List;

import board.BoardKoRuleViolatedE;
import server.Server;
import serverView.ToClientPacket;

public class GameObj {
	
	private int gameId;
	
	private PlayerObj playerObj1 = null;
	private PlayerObj playerObj2 = null;
	
	// Settings of P1 and P2
	private String p1Colour = null; // (P2 follows the inverse, cannot be equal)
	private String p2Colour = null;
	
	// Feeder: flips to true if pass is hit. 2nd time PASS is then observable.
	// MOVE makes it false
	private Boolean p1PassState = false;
	private Boolean p2PassState = false;
	
	private int boardSize = 0; // is converted string to int
	
	// System preferences
	
	private String p1DefaultColour = "BLACK";
	private String p2DefaultColour = "WHITE";
	// If the preferred colour of P1 is black
	private String p2DefaultColourTaken = "ORANGE";
	private String defaultBoardSize = "13";
	
	//Delimiter 1&2 (! no connection)
	private String delimiter1 = Server.getDELIMITER1();
	private String delimiter2 = Server.getDELIMITER2();
	
	// Board type
	board.Board board;
	
	// Queue for this game (reference to txQueue of gameManager (final))
	private List<ToClientPacket> gameTxQueue;

	private int hasTurnClientId;
	
    /**
     * The gameobject connects the players to the game objects via methods.
     * It also handles error messages and is able to send network messages.
     * 
     * If no player has configured settings defaults are taken.
     * The board is initialized.
     * A gui is initialized.
     * 
     * @param clientId	The clientId of the target
     * @param skt		socket object "socket.accept()'ed"
     * @see 
     */
	public GameObj(int gameId, List<ToClientPacket> txQueue, PlayerObj p1, PlayerObj p2) {
		
		this.playerObj1 = p1;
		this.playerObj2 = p2;
		this.hasTurnClientId = p1.getClientId();
		
		this.gameTxQueue = txQueue;
		this.setGameId(gameId);
		
		// Boot up new game		
		p1.setIsInGame(true); messageP1("OTHER",
				"Playerstatus: Ingame as Player 1! Good to see you, " + getP1().getName());
		p2.setIsInGame(true); messageP2("OTHER",
				"Playerstatus: Ingame as Player 2! Good to see you, " + getP2().getName());
		
		// Setting up boardSize
		if (p1.getSettingBoardSize() == null) {
			
			messageBoth("OTHER",
							"P1 has no preference: boardSize = "
							+ defaultBoardSize);
			this.boardSize = Integer.parseInt(defaultBoardSize);
			
		} else {
			try {
				this.boardSize = Integer.parseInt(p1.getSettingBoardSize());
			} catch (NumberFormatException e) {
				messageBoth("OTHER",
						"P1 has non-int preference: boardSize = "
								+ defaultBoardSize);
				this.boardSize = Integer.parseInt(defaultBoardSize);
			}
		}
		
		// Setting up colour
		if (p1.getSettingColour() == null) {
			
			messageBoth("OTHER",
						"P1 has no preference: colour = "
						+ p1DefaultColour);
			this.p1Colour = p1DefaultColour;
			
		} else {
			
			this.p1Colour = p1.getSettingColour();
		}
		
		if (null != p2.getSettingColour()) {
			//Set along P1 p2 user pref try
			if (p2.getSettingColour().equals(this.p1Colour)) {
				
				this.p2Colour = p2.getSettingColour();
				
			} else {
				
				p2.setSettingColour(null);
				// Triggers the block below
			}
		}
		
		// Now give P2 a chance to set his favorite colour
		if (p2.getSettingColour() == null) {
			
			//Set along P1 default
			if (this.p2DefaultColour.equals(this.p1Colour)) {
				
				this.p2Colour = this.p2DefaultColourTaken;
				
			} else {
				
				this.p2Colour = this.p2DefaultColour;
			}
			
		}
		
		messageBoth("OTHER",
				"P2 got colour:" + this.p2Colour);
		
		// Create board
		
		this.board = new board.Board(this.boardSize);
		
		messageBoth("INFO", "\n" + board.toStringClient());
		
		// Send out START protocol
		//messageBoth("START", 
		//		"2");
		
		messageBoth("START", 
				 "2" + delimiter1 + this.p1Colour + delimiter1 + this.boardSize
				+ delimiter1 + p1.getName() + delimiter1 + p2.getName());
		// Send out TURN protocol
		messageBoth("TURN", 
				getPlayerNameOf(this.hasTurnClientId)
				+ delimiter1 + 
				"FIRST" + delimiter1 + getPlayerNameOf(this.hasTurnClientId));
		
	}
	
	public int getBoardSize() {
		return boardSize;
	}
	
	public board.Board getBoard() {
		return board;
	}

	public PlayerObj getPlayerObj(int clientId) {
		PlayerObj playerObj = null; 
		if (clientId == playerObj1.getClientId()) {
			
			playerObj = getP1();
			
		} else if (clientId == playerObj2.getClientId()) {
			
			playerObj = getP2();
		}
		
		return playerObj;
	}
	
	public int getPlayerNumber(int clientId) {
		
		int playerId = 0; 
		if (clientId == playerObj1.getClientId()) {
			
			playerId = 1;
			
		} else if (clientId == playerObj2.getClientId()) {
			
			playerId = 2;
		}
		return playerId;
		
	}
	
	public Boolean getPlayerPassState(int clientId) {
		
		Boolean passState = null; 
		if (clientId == playerObj1.getClientId()) {
			
			passState = p1PassState;
			
		} else if (clientId == playerObj2.getClientId()) {
			
			passState = p2PassState;
		}
		return passState;
		
	}
	
	public Boolean getOtherPlayerPassState(int clientId) {
		
		Boolean passState = null; 
		if (clientId == playerObj2.getClientId()) {
			
			passState = p1PassState;
			
		} else if (clientId == playerObj1.getClientId()) {
			
			passState = p2PassState;
		}
		return passState;
		
	}
	
	public void setPlayerPassState(int clientId, Boolean passState) {
		
		if (clientId == playerObj1.getClientId()) {
			
			p1PassState = passState;
			
		} else if (clientId == playerObj2.getClientId()) {
			
			p2PassState = passState;
		}
		
	}
	
	public String getNextPlayerNameOf(int clientId) {
		
		String nextPlayerName = null;
		//If you type in P1 it gets P2.
		if (getPlayerNumber(clientId) == 1) {
			// If self is 1, return 2
			nextPlayerName = playerObj2.getName();
					
		} else if (getPlayerNumber(clientId) == 2) {
			// If self is 2, return 1
			nextPlayerName = playerObj1.getName();
		}
		
		return nextPlayerName;
	}
	
	public int getNextPlayerClientIdOf(int clientId) {
		
		int nextPlayerClientId = 0;
		//If you type in P1 it gets P2.
		if (getPlayerNumber(clientId) == 1) {
			// If self is 1, return 2
			nextPlayerClientId = playerObj2.getClientId();
					
		} else if (getPlayerNumber(clientId) == 2) {
			// If self is 2, return 1
			nextPlayerClientId = playerObj1.getClientId();
		}
		
		return nextPlayerClientId;
	}
	
	
	
	public String getPlayerNameOf(int clientId) {
		
		String playerName = null;
		//If you type in P1 it gets P2.
		if (getPlayerNumber(clientId) == 1) {
			// If self is 1, return 2
			playerName = playerObj1.getName();
					
		} else if (getPlayerNumber(clientId) == 2) {
			// If self is 2, return 1
			playerName = playerObj2.getName();
		}
		
		return playerName;
		
	}
	
    /**
     * The move is executed if it is valid.
     * In order to establish the validity of the move the board
     * object (final) associated to the game is used to cather out the move.
     * 
     * @param clientId	The clientId of the target
     * @param row		row of the move position
     * @param col		collumn of the move position
     * @see 
     */
	
	public void doMoveForPlayer(int clientId, int row, int col) {
		
		if (clientId == this.hasTurnClientId) {
			if (board.isMoveValid(getPlayerNumber(clientId), row, col)) {
				
				try {
					board.putStoneForPlayer(getPlayerNumber(clientId), row, col, true, false);
					System.out.println("P1 Score:" + board.getScoreP1());
					System.out.println("P2 Score:" + board.getScoreP2());
					
					// Answer to client $TURN$nextPlayerName$row_col$currentPlayerName
					//messageBoth("INFO", "\n" + board.toStringClient());
					
					messageBoth("TURN",
							getPlayerNameOf(this.hasTurnClientId)
							+ delimiter1
							+ row
							+ delimiter2
							+ col
							+ delimiter1
							+ getNextPlayerNameOf(this.hasTurnClientId));
					
					// Reset pass
					setPlayerPassState(clientId, false);
					
					// Switch turn (depending on implementation)
					this.hasTurnClientId = getNextPlayerClientIdOf(this.hasTurnClientId);
				} catch (BoardKoRuleViolatedE e) {
					messageClientId(clientId, "INVALIDMOVE", "Ko rule violation!");
					
				}
				
				
			} else {
				// Wrong
				messageClientId(clientId, "INVALIDMOVE", "Invalid Move !");	
			}
		} else {
			
			messageClientId(clientId, "ERROR", "Arr, wait yer turn");
			
		}
		
	}
	
    /**
     * This method determines if a consequetive pass is is done.
     * If so the game can be quit.
     * 
     * @param clientId	The clientId of the target
     */
	
	public Boolean passForPlayer(int clientId) {
		
		Boolean doPassQuit = false;
		
		if (clientId == this.hasTurnClientId) {
			
			// /Other Player did already pass once:
			if (getOtherPlayerPassState(clientId)) {
				
				doPassQuit = true;
			
			} else {
				
				// Answer to client $TURN$nextPlayerName$row_col$currentPlayerName
				//messageBoth("INFO",
				//		"\n" 
				//		+ board.toStringClient());
				
				messageBoth("TURN",
						getPlayerNameOf(this.hasTurnClientId)
						+ delimiter1 
						+ "PASS" 
						+ delimiter1
						+ getNextPlayerNameOf(this.hasTurnClientId));

				// Switch turn (depending on implementation)
				this.hasTurnClientId = getNextPlayerClientIdOf(this.hasTurnClientId);
				
				setPlayerPassState(clientId, true);
				
			}
				
		} else {
			
			messageClientId(clientId, "ERROR", "Arr, wait yer turn to PASS");
			
		}
		
		return doPassQuit;
		
	}
	
	public void messageClientId(int clientId, String cmd, String line) {
		
		gameTxQueue.add(new ToClientPacket(clientId, cmd, line));
		
	}
	
	public void messageP1(String cmd, String line) {
		
		gameTxQueue.add(new ToClientPacket(playerObj1.getClientId(), cmd, line));
		
	}
	
	public void messageP2(String cmd, String line) {
		
		gameTxQueue.add(new ToClientPacket(playerObj2.getClientId(), cmd, line));
	}
	
	public void messageBoth(String cmd, String line) {
		
		messageP1(cmd, line);
		messageP2(cmd, line);
	}
	
	public PlayerObj getP1() {
		return playerObj1;
	}

	public void setP1(PlayerObj p1) {
		playerObj1 = p1;
	}

	public PlayerObj getP2() {
		return playerObj2;
	}

	public void setP2(PlayerObj p2) {
		playerObj2 = p2;
	}

	public int getGameId() {
		return gameId;
	}

	public void setGameId(int gameId) {
		this.gameId = gameId;
	}

}
