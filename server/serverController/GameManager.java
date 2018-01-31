package serverController;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import serverModel.GameObj;
import serverView.ToClientPacket;
import serverView.ToServerPacket;

public class GameManager {
	
	private int gameId = 0;
	final List<GameObj> games = new ArrayList<>();
	final List<ToServerPacket> requestQueue = new ArrayList<>();
	final List<ToClientPacket> txQueue = new ArrayList<>();
	private PlayerManager playMan;
	
    /**
     * The GameManager is a storage of the currently playing games.
     * It is connected with the clients via Lists, which are to be maintained by the Server class.
     * 
     * 
     * @param	toRemoveClientId	The toRemoveClientId of the player
     * @return	void
     */
	
	public GameManager(PlayerManager playMan) {
		
		this.playMan = playMan;
		
	}
	
    /**
     * A client who wants to join a game is added onto the request queue.
     * 
     * @param	toServerPacket		the ToServerPacket of the player wanting to join.
     * @return	void
     */
	
	public void addToRequestQueue(ToServerPacket toServerPacket) {
		
		requestQueue.add(toServerPacket);
		
	}
	
    /**
     * A client that needs to be removed from the requestQueue is removed here.
     * 
     * @param	toRemoveClientId	The toRemoveClientId of the player
     * @return	void
     */
	
	public void removeFromRequestQueue(int toRemoveClientId) {
		
		requestQueue.removeIf(toServerPacket -> {
		    return toServerPacket.getClientId() == toRemoveClientId;
		});
	}
	
    /**
     * For a move specified of a player, the gameObj of the current game is grabbed
     * afterwards the gameObj is invoked to do the move.
     * 
     * @param	clientId	The clientId of the player which wants to pass
     * @return	void
     * 
     * 
     */
	
	public void tryMoveFor(int clientId, int moveDataRow, int moveDataCol) {
		
		try {
			GameObj gameObj = getGameObjForClient(clientId);
			
			if (null != gameObj) {
				
				gameObj.doMoveForPlayer(clientId, moveDataRow, moveDataCol);
				
			} else {
				
				System.out.println("move: gameobject is null");
				// Maybe want to quit the game here ?
			}
		} catch (NullPointerException e) {
			
			// silent
			// Maybe want to quit the game here ?
		}
		
	}
	
    /**
     * Accepts a pass, and determines with it if the game should end or not.
     * 
     * @param	clientId	The clientId of the player which wants to pass
     * @return	void
     * 
     * 
     */
	
	public void passFor(int clientId) {
		
		try {
			GameObj gameObj = getGameObjForClient(clientId);
			
			if (null != gameObj) {
				
				// A player can quit by 2 subsequent passes.
				// This is kept in the logic of the return value of passForPlayer.
				// True = pass & quit. False = pass
				
				if (gameObj.passForPlayer(clientId)) {
					
					this.quit2PGameFor(clientId);
					
				}
				
			} else {
				
				System.out.println("pass: gameobject is null");
				// Maybe want to quit the game here
			}
		} catch (NullPointerException e) {
			
			// silent
			// Maybe want to quit the game here ?
		}
		
	}
	
    /**
     * Error handling Quit Game function, in any occurence 
     * (for example socket error) a game and its players
     * should be cleaned from it.
     * 
     * @param	clientId	The clientId of the player
     * @return	gameObj		The gameObj in which the player is in.
     * 
     */
	public void errorQuit2PGameFor(int clientId) {
		
		// Switch made to deduce P1 or P2
		
		GameObj errorToQuitGameObj = games.stream()
				.filter(p -> p.getP1().getClientId() == clientId)
				.findFirst()
				.orElse(null);
		
		if (null == errorToQuitGameObj) {
			
			errorToQuitGameObj = games.stream()
					.filter(p -> p.getP2().getClientId() == clientId)
					.findFirst()
					.orElse(null);
			
			if (null != errorToQuitGameObj) {
				// This query of grabbing P2 should give a result
				errorToQuitGameObj.messageP1("ENDGAME", "QUIT cmd given by:"
						+ playMan.getPlayerName(clientId));
			}

		} else {
				// Now signal that the game is being destroyed
			errorToQuitGameObj.messageP2("ENDGAME", "QUIT cmd given by:"
						+ playMan.getPlayerName(clientId));
		}
		
		// Because of the search method above, if the errorToQuitGameObj stays null
		// Conclusion: the player wasn't playing, some kind of error. Clean.
		
		if (null != errorToQuitGameObj) {
			final int toRemoveGameId = errorToQuitGameObj.getGameId();
			games.removeIf(gameObj -> {
			    return gameObj.getGameId() == toRemoveGameId;
			});
			
			// Kick both out of the inGame status
			errorToQuitGameObj.getP1().setIsInGame(false);
			errorToQuitGameObj.getP2().setIsInGame(false);
		
		}
		
	}
	
    /**
     * Searches for any game a player is currently participating in.
     * 
     * @param	clientId	The clientId of the player
     * @return	gameObj		The gameObj in which the player is in.
     * 
     */
	
	public GameObj getGameObjForClient(int clientId) {
		
		GameObj gameObj = games.stream()
				.filter(p -> p.getP1().getClientId() == clientId)
				.findFirst()
				.orElse(null);
		
		if (null == gameObj) {
			
			gameObj = games.stream()
					.filter(p -> p.getP2().getClientId() == clientId)
					.findFirst()
					.orElse(null);
			
		}
		
		return gameObj;
		
	}
	
    /**
     * The method searches for any games (as player 1 or player 2)
     * the clientId is participating in.
     * It subsequently searches a game, then removes it from the 'memory' list.
     * Afterwards the players are unset of their ingame status and informed about the quit.
     * 
     * @param	clientId	The clientId of the player
     * @return	void
     * 
     * @see server.Server.playerIsInGameServlet(), passFor();
     */
	
	public void quit2PGameFor(int clientId) {
		
		GameObj toQuitGameObj = getGameObjForClient(clientId);
		
		// Scoring
		// Communicate the highest score first.
		if (toQuitGameObj.getBoard().getScoreP1() >=
				toQuitGameObj.getBoard().getScoreP2()) {
			// Now signal that the game is being destroyed
			toQuitGameObj.messageBoth("ENDGAME",
					"QUIT"
					+ server.Server.getDELIMITER1()
					+ toQuitGameObj.getP1().getName()
					+ server.Server.getDELIMITER1()
					+ toQuitGameObj.getBoard().getScoreP1()
					+ server.Server.getDELIMITER1()
					+ toQuitGameObj.getP2().getName()
					+ server.Server.getDELIMITER1()
					+ toQuitGameObj.getBoard().getScoreP2());
			
		} else {
			
			// Now signal that the game is being destroyed
			toQuitGameObj.messageBoth("ENDGAME",
					"QUIT"
					+ server.Server.getDELIMITER1()
					+ toQuitGameObj.getP2().getName()
					+ server.Server.getDELIMITER1()
					+ toQuitGameObj.getBoard().getScoreP2()
					+ server.Server.getDELIMITER1()
					+ toQuitGameObj.getP1().getName()
					+ server.Server.getDELIMITER1()
					+ toQuitGameObj.getBoard().getScoreP1());
			
		}
		
		
		final int toRemoveGameId = toQuitGameObj.getGameId();
		games.removeIf(gameObj -> {
		    return gameObj.getGameId() == toRemoveGameId;
		});
		
		// Kick both out of the inGame status
		toQuitGameObj.getP1().setIsInGame(false);
		toQuitGameObj.getP2().setIsInGame(false);
		


	}
	
    /**
     * The method creates a new GameObject and assigns two players.
     * The players are removed after assignment from the requestQueue.
     * An unique game-id is assigned to the created game, the counter is held in
     * this.gameId.
     * 
     * @param	packetP1	ToServerPacket which contains the game request
     * @param 	packetP2	ToServerPacket which contains the game request
     * @return	void
     * 
     * @see processRequestQueue();
     */
	public void new2PGame(ToServerPacket packetP1, ToServerPacket packetP2) {
		
		this.gameId = this.gameId + 1;
		// Create new game plus an instance of the txQueue of GameManager
		// Passing a new gameObj
		// with params: txQueue, getPlayerObj1, getPlayerObj2
		games.add(new GameObj(this.gameId, txQueue,
				playMan.getPlayerObj(packetP1.getClientId()),
				playMan.getPlayerObj(packetP2.getClientId()))
		);
		
		// Remove from queue
		// Bool return means remove P1 and P2 if "this element" equals one of the 2
		requestQueue.removeIf(i -> {
		    return i.getClientId() == packetP1.getClientId();
		});
		
		requestQueue.removeIf(i -> {
		    return i.getClientId() == packetP2.getClientId();
		});
	}
	
    /**
     * Processing of the request queue occurs here.
     * The list requestQueue is retained if the amount of players is not 2.
     * When more players are enqueued, they are assigned a game based first in
     * first out.
     * 
     * If a player is assigned not assigned a game a message is sent to the client
     * via the message queue txQueue, the message queue of GameManager.
     * 
     * @param	
     * @return	void
     * 
     * @see txQueue.add(), new2PGame();
     */
	
	public void processRequestQueue() {
		List<ToServerPacket> reqQueue = requestQueue.stream()
				.sorted((f1, f2) -> Long.compare(f1.getStartTime(), f2.getStartTime())).
                collect(Collectors.toList());
		// Put all messages in queue. Will be sent out in step 4 alltogether in main.
		
		// This is because of the iterator/foreach needing a 'final'
		ToServerPacket[] playerPacket1 = new ToServerPacket[1];
		ToServerPacket[] playerPacket2 = new ToServerPacket[1];
		playerPacket1[0] = null;
		playerPacket2[0] = null;
		
		reqQueue.forEach((cToServerPacket) -> {
			
			// First in queue
			if (playerPacket1[0] == null) {
				
				playerPacket1[0] = cToServerPacket;
				txQueue.add(new ToClientPacket(cToServerPacket.getClientId(),
						"INFO",
						"Put in waiting slot. You are P1! You can use the command"
						+ "SETTINGS$ to set the game preferences while you wait :)."));
				
			} else if (playerPacket2[0] == null) {
			
				if (playerPacket1[0].getClientId() != cToServerPacket.getClientId()) {
									
					playerPacket2[0] = cToServerPacket;			
									
				} else {
					
					txQueue.add(new ToClientPacket(cToServerPacket.getClientId(),
							"INFO",
							"You are already in request of a game as P1."));
					
				}
			}
			
			// New game
			
			if (playerPacket1[0] != null && playerPacket2[0] != null) {
				
				new2PGame(playerPacket1[0], playerPacket2[0]);
				
				// clear matching queue
				playerPacket1[0] = null;
				playerPacket2[0] = null;
				
			}
			
		});
		
	}
	
	/**
	 * Getter for txQueue.
	 * @return 	txQueue List<ToClientPacket>
	 */
	
	public List<ToClientPacket> getTxQueue() {
		return txQueue;
	}
	
	/** 
	 * Clears txQueue list.
	 */
	public void clearTxQueue() {
		
		txQueue.clear();
	}

	
}