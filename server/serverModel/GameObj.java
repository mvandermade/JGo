package serverModel;

import java.util.List;

import board.BoardKoRuleViolated;

public class GameObj {
	
	private int gameId;
	
	private PlayerObj P1 = null;
	private PlayerObj P2 = null;
	
	// Settings of P1 and P2
	private String P1Colour = null; // (P2 follows the inverse, cannot be equal)
	private String P2Colour = null;
	
	// Feeder: flips to true if pass is hit. 2nd time PASS is then observable.
	// MOVE makes it false
	private Boolean P1PassState = false;
	private Boolean P2PassState = false;
	
	private int boardSize = 0; // is converted string to int
	
	// System preferences
	
	private String P1DefaultColour = "BLACK";
	private String P2DefaultColour = "WHITE";
	// If the preferred colour of P1 is black
	private String P2DefaultColourTaken = "ORANGE";
	private String defaultBoardSize = "13";
	
	//Delimiter 1&2 (! no connection)
	private String D1 = "$";
	private String D2 = "_";
	
	// 
	
	// Board type
	board.Board board;
	
	// Queue for this game (reference to txQueue of gameManager (final))
	private List<ToClientPacket> gameTxQueue;

	private int hasTurnClientId;
	
	
	GameObj(int gameId, List<ToClientPacket> txQueue, PlayerObj P1, PlayerObj P2) {
		
		this.P1 = P1;
		this.P2 = P2;
		this.hasTurnClientId = P1.getClientId();
		
		this.gameTxQueue = txQueue;
		this.setGameId(gameId);
		
		// Boot up new game		
		P1.setIsInGame(true); messageP1("OTHER","Playerstatus: Ingame as Player 1! Good to see you, "+getP1().getName());
		P2.setIsInGame(true); messageP2("OTHER","Playerstatus: Ingame as Player 2! Good to see you, "+getP2().getName());
		
		// Setting up boardSize
		if (P1.getSettingBoardSize() == null) {
			
			messageBoth("OTHER","P1 has no preference: boardSize = "+defaultBoardSize);
			this.boardSize = Integer.parseInt(defaultBoardSize);
			
		} else {
			try {
				this.boardSize = Integer.parseInt(P1.getSettingBoardSize());
			} catch (NumberFormatException e) {
				messageBoth("OTHER","P1 has non-int preference: boardSize = "+defaultBoardSize);
				this.boardSize = Integer.parseInt(defaultBoardSize);
			}
		}
		
		// Setting up colour
		if (P1.getSettingColour() == null) {
			
			messageBoth("OTHER","P1 has no preference: colour = "+P1DefaultColour);
			this.P1Colour = P1DefaultColour;
			
		} else {
			
			this.P1Colour = P1.getSettingColour();
		}
		
		if (null != P2.getSettingColour()) {
			//Set along P1 p2 user pref try
			if (P2.getSettingColour().equals(this.P1Colour)) {
				
				this.P2Colour = P2.getSettingColour();
				
			} else {
				
				P2.setSettingColour(null);
				// Triggers the block below
			}
		}
		
		// Now give P2 a chance to set his favorite colour
		if (P2.getSettingColour() == null) {
			
			//Set along P1 default
			if (this.P2DefaultColour.equals(this.P1Colour)) {
				
				this.P2Colour = this.P2DefaultColourTaken;
				
			} else {
				
				this.P2Colour = this.P2DefaultColour;
			}
			
		}
		
		messageBoth("OTHER","P2 got colour:"+this.P2Colour);
		
		// Create board
		
		this.board = new board.Board(this.boardSize);
		
		messageBoth("INFO", "\n" + board.toStringClient());
		
		// Send out START protocol
		messageBoth("START",this.P1Colour+D1+this.boardSize);
		// Send out TURN protocol
		messageBoth("TURN", getPlayerNameOf(this.hasTurnClientId)+D1+"FIRST"+D1+getPlayerNameOf(this.hasTurnClientId));
		
	}
	
	public int getBoardSize() {
		return boardSize;
	}

	public PlayerObj getPlayerObj(int clientId) {
		PlayerObj playerObj = null; 
		if (clientId == P1.getClientId()) {
			
			playerObj = getP1();
			
		} else if (clientId == P2.getClientId()) {
			
			playerObj = getP2();
		}
		
		return playerObj;
	}
	
	public int getPlayerNumber(int clientId) {
		
		int playerId = 0; 
		if (clientId == P1.getClientId()) {
			
			playerId = 1;
			
		} else if (clientId == P2.getClientId()) {
			
			playerId = 2;
		}
		return playerId;
		
	}
	
	public Boolean getPlayerPassState(int clientId) {
		
		Boolean passState = null; 
		if (clientId == P1.getClientId()) {
			
			passState = P1PassState;
			
		} else if (clientId == P2.getClientId()) {
			
			passState = P2PassState;
		}
		return passState;
		
	}
	
	public Boolean getOtherPlayerPassState(int clientId) {
		
		Boolean passState = null; 
		if (clientId == P2.getClientId()) {
			
			passState = P1PassState;
			
		} else if (clientId == P1.getClientId()) {
			
			passState = P2PassState;
		}
		return passState;
		
	}
	
	public void setPlayerPassState(int clientId, Boolean passState) {
		
		if (clientId == P1.getClientId()) {
			
			P1PassState = passState;
			
		} else if (clientId == P2.getClientId()) {
			
			P2PassState = passState;
		}
		
	}
	
	
	
	public String getNextPlayerNameOf(int clientId) {
		
		String nextPlayerName = null;
		//If you type in P1 it gets P2.
		if (getPlayerNumber(clientId) == 1) {
			// If self is 1, return 2
			nextPlayerName = P2.getName();
					
		} else if (getPlayerNumber(clientId) == 2) {
			// If self is 2, return 1
			nextPlayerName = P1.getName();
		}
		
		return nextPlayerName;
	}
	
	public int getNextPlayerClientIdOf(int clientId) {
		
		int nextPlayerClientId = 0;
		//If you type in P1 it gets P2.
		if (getPlayerNumber(clientId) == 1) {
			// If self is 1, return 2
			nextPlayerClientId = P2.getClientId();
					
		} else if (getPlayerNumber(clientId) == 2) {
			// If self is 2, return 1
			nextPlayerClientId = P1.getClientId();
		}
		
		return nextPlayerClientId;
	}
	
	
	
	public String getPlayerNameOf(int clientId) {
		
		String PlayerName = null;
		//If you type in P1 it gets P2.
		if (getPlayerNumber(clientId) == 1) {
			// If self is 1, return 2
			PlayerName = P1.getName();
					
		} else if (getPlayerNumber(clientId) == 2) {
			// If self is 2, return 1
			PlayerName = P2.getName();
		}
		
		return PlayerName;
		
	}
	
	public void doMoveForPlayer(int clientId, int row, int col) {
		
		
		if(clientId == this.hasTurnClientId) {
			if(board.isMoveValid(getPlayerNumber(clientId), row, col)) {
				
				int rowplus1 = row + 1;
				int colplus1 = col + 1;
				
				try {
					board.putStoneForPlayer(getPlayerNumber(clientId), row, col);
					System.out.println("P1 Score:"+board.getScoreP1());
					System.out.println("P2 Score:"+board.getScoreP2());
					
					// Answer to client $TURN$nextPlayerName$row_col$currentPlayerName
					messageBoth("INFO", "\n" + board.toStringClient());
					
					messageBoth("TURN", getNextPlayerNameOf(this.hasTurnClientId)+D1+rowplus1+D2+colplus1+D1+getPlayerNameOf(this.hasTurnClientId));
					
					// Reset pass
					setPlayerPassState(clientId, false);
					
					// Switch turn (depending on implementation)
					this.hasTurnClientId = getNextPlayerClientIdOf(this.hasTurnClientId);
				} catch (BoardKoRuleViolated e) {
					messageClientId(clientId, "ERROR", "Ko rule violation!");
					
				}
				
				
			} else {
				// Wrong
				messageClientId(clientId, "ERROR", "Invalid Move !");
				//messageClientId(clientId, "TURN", getPlayerNameOf(this.hasTurnClientId)+D1+row+D2+col+D1+getNextPlayerNameOf(this.hasTurnClientId));
	
			}
		} else {
			
			messageClientId(clientId, "ERROR", "Arr, wait yer turn");
			
		}
		
	}
	
	public Boolean passForPlayer(int clientId) {
		
		Boolean doPassQuit = false;
		
		if(clientId == this.hasTurnClientId) {
			
			// /Other Player did already pass once:
			if(getOtherPlayerPassState(clientId)) {
				
				doPassQuit = true;
			
			} else {
				
				// Answer to client $TURN$nextPlayerName$row_col$currentPlayerName
				messageBoth("INFO", "\n" + board.toStringClient());
				
				messageBoth("TURN", getNextPlayerNameOf(this.hasTurnClientId)+D1+"PASS"+D1+getPlayerNameOf(this.hasTurnClientId));
				
				
				
				// Switch turn (depending on implementation)
				this.hasTurnClientId = getNextPlayerClientIdOf(this.hasTurnClientId);
				
				setPlayerPassState(clientId, true);
				
			}
				
		} else {
			
			messageClientId(clientId, "ERROR", "Arr, wait yer turn to PASS");
			
		}
		
		return doPassQuit;
		
	}
	
	public void messageClientId(int clientId, String CMD, String line) {
		
		gameTxQueue.add(new ToClientPacket(clientId, CMD, line));
		
	}
	
	public void messageP1(String CMD, String line) {
		
		gameTxQueue.add(new ToClientPacket(P1.getClientId(), CMD, line));
		
	}
	
	public void messageP2(String CMD, String line) {
		
		gameTxQueue.add(new ToClientPacket(P2.getClientId(), CMD, line));
	}
	
	public void messageBoth(String CMD, String line) {
		
		messageP1(CMD, line);
		messageP2(CMD, line);
	}
	
	public PlayerObj getP1() {
		return P1;
	}

	public void setP1(PlayerObj p1) {
		P1 = p1;
	}

	public PlayerObj getP2() {
		return P2;
	}

	public void setP2(PlayerObj p2) {
		P2 = p2;
	}

	public int getGameId() {
		return gameId;
	}

	public void setGameId(int gameId) {
		this.gameId = gameId;
	}

}
