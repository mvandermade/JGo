package serverModel;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GameManager {
	
	private int gameId = 0;
	final List<GameObj> games = new ArrayList<>();
	final List<ToServerPacket> requestQueue = new ArrayList<>();
	final List<ToClientPacket> txQueue = new ArrayList<>();
	private PlayerManager playMan;
	
	public GameManager(PlayerManager playMan) {
		
		this.playMan = playMan;
		
	}
	
	public void addToRequestQueue(ToServerPacket cRx) {
		
		requestQueue.add(cRx);
		
	}
	
	public void removeFromRequestQueue(int toRemoveClientId) {
		
		requestQueue.removeIf( toServerPacket -> {
		      return toServerPacket.getClientId() == toRemoveClientId;//No return statement will break compilation
		    });
	}
	
	public void tryMoveFor(int clientId, int moveDataRow, int moveDataCol) {
		
		try {
			GameObj gameObj = getGameObjForClient(clientId);
			
			if (null != gameObj) {
				
				gameObj.doMoveForPlayer(clientId, moveDataRow, moveDataCol);
				
			} else {
				
				System.out.println("move: gameobject is null @GameManager row 38");
			}
		} catch (NullPointerException e){
			
			e.printStackTrace();
		}
		
	}
	
	public void passFor(int clientId) {
		
		try {
			GameObj gameObj = getGameObjForClient(clientId);
			
			if (null != gameObj) {
				
				if (gameObj.passForPlayer(clientId)) {
					
					this.quit2PGameFor(clientId);
					
				};
				
			} else {
				
				System.out.println("move: gameobject is null @GameManager row 38");
			}
		} catch (NullPointerException e) {
			
			e.printStackTrace();
		}
		
	}
	
	public void errorQuit2PGameFor(int clientId) {
		
		// Switch made to deduce P1 or P2
		
		GameObj errorToQuitGameObj = games.stream()
				.filter(p -> p.getP1().getClientId()==clientId)
				.findFirst()
				.orElse(null);
		
		if (null == errorToQuitGameObj) {
			
			errorToQuitGameObj = games.stream()
					.filter(p -> p.getP2().getClientId()==clientId)
					.findFirst()
					.orElse(null);
			
			if (null != errorToQuitGameObj) {
				// This query of grabbing P2 should give a result
				errorToQuitGameObj.messageP1("ENDGAME", "QUIT cmd given by:"+playMan.getPlayerName(clientId));
				
			}

		} else {
			
			// Now signal that the game is being destroyed
			errorToQuitGameObj.messageP2("ENDGAME", "QUIT cmd given by:"+playMan.getPlayerName(clientId));

		}
		
		// Because of the search method above, if the errorToQuitGameObj stays null
		// Conclusion: the player wasn't playing!
		
		if (null != errorToQuitGameObj) {
			final int toRemoveGameId = errorToQuitGameObj.getGameId();
			games.removeIf( gameObj -> {
			      return gameObj.getGameId() == toRemoveGameId;//No return statement will break compilation
			    });
			
			// Kick both out of the inGame status
			errorToQuitGameObj.getP1().setIsInGame(false);
			errorToQuitGameObj.getP2().setIsInGame(false);
		
		}
		
	}
	
	public GameObj getGameObjForClient(int clientId) {
		
		GameObj gameObj = games.stream()
				.filter(p -> p.getP1().getClientId()==clientId)
				.findFirst()
				.orElse(null);
		
		if (null == gameObj) {
			
			gameObj = games.stream()
					.filter(p -> p.getP2().getClientId()==clientId)
					.findFirst()
					.orElse(null);
			
		}
		
		return gameObj;
		
	}
	
	public void quit2PGameFor(int clientId) {
		
		GameObj toQuitGameObj = getGameObjForClient(clientId);
		
		final int toRemoveGameId = toQuitGameObj.getGameId();
		games.removeIf( gameObj -> {
		      return gameObj.getGameId() == toRemoveGameId;//No return statement will break compilation
		    });
		
		// Kick both out of the inGame status
		toQuitGameObj.getP1().setIsInGame(false);
		toQuitGameObj.getP2().setIsInGame(false);
		
		// Now signal that the game is being destroyed
		toQuitGameObj.messageBoth("ENDGAME", "QUIT cmd given by:"+playMan.getPlayerName(clientId));

		
	}
	
	
	public void new2PGame(ToServerPacket P1, ToServerPacket P2) {
		
		this.gameId = this.gameId + 1;
		
		// Create new game plus an instance of the txQueue of GameManager
		// Passing a new gameObj
		// with params: txQueue, getPlayerObj1, getPlayerObj2
		games.add(new GameObj(this.gameId, txQueue, playMan.getPlayerObj(P1.getClientId()), playMan.getPlayerObj(P2.getClientId()) ) );
		
		// Remove from queue
		// bool return means remove P1 and P2 if "this element" equals one of the 2
		requestQueue.removeIf( i -> {
		      return i.getClientId() == P1.getClientId();//No return statement will break compilation
		    });
		
		requestQueue.removeIf( i -> {
		      return i.getClientId() == P2.getClientId();//No return statement will break compilation
		    });
		
		
	}
	
	public void processRequestQueue() {
	
	
		List<ToServerPacket> reqQueue = requestQueue.stream()
				.sorted((f1, f2) -> Long.compare(f1.getStartTime(), f2.getStartTime())).
                collect(Collectors.toList());
		// Put all messages in queue. Will be sent out in step 4 alltogether in main.
		
		// This is because of the iterator/foreach needing a 'final'
		ToServerPacket[] P1 = new ToServerPacket[1];
		ToServerPacket[] P2 = new ToServerPacket[1];
		P1[0] = null;
		P2[0] = null;
		
		reqQueue.forEach((cToServerPacket)->{
			
			// First in queue
			if (P1[0] == null) {
				
				P1[0] = cToServerPacket;
				txQueue.add(new ToClientPacket(cToServerPacket.getClientId(), "INFO","Put in waiting slot. You are P1! You can use the command SETTINGS$ to set the game preferences while you wait :)."));
				
			} else if(P2[0]==null) {
			
				if (P1[0].getClientId() != cToServerPacket.getClientId()) {
									
					P2[0] = cToServerPacket;			
									
				} else {
					
					txQueue.add(new ToClientPacket(cToServerPacket.getClientId(), "INFO","You are already in request of a game as P1."));
					
				}
			}
			
			// New game
			
			if (P1[0]!=null && P2[0]!=null) {
				
				new2PGame(P1[0], P2[0]);
				
				// clear matching queue
				P1[0] = null;
				P2[0] = null;
				
			}
			
		});
		
	}
	
	// For connMan
	public List<ToClientPacket> getTxQueue() {
		return txQueue;
	}
	
	public void clearTxQueue() {
		
		txQueue.clear();
	}

	
}