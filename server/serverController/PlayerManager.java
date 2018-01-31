package serverController;

import java.util.ArrayList;
import java.util.List;

import serverModel.PlayerObj;

public class PlayerManager {
	
	final List<PlayerObj> players = new ArrayList<>();
	
    /**
     * PlayerManager contains a list of Players and provides methods 
     * to do operations on the player objects stored in them.
     */
	
	public PlayerManager() {
		// Nothing necessary
	}
	
    /**
     * Player is added if it doesn't exist.
     * 
     * @param clientId	The clientId of the target
     * @param name		The requested name for the clientId
     * @see 
     */
	
	public void addPlayer(int clientId, String name) {
				
		boolean pAlreadyExists = players.stream()
		            .anyMatch(p -> p.getClientId() == clientId);
			
		if (!pAlreadyExists) {
			players.add(new PlayerObj(clientId, name));
		}
		
	}
	
    /**
     * Returns player name string belonging to clientId.
     * 
     * @param clientId	The clientId of the target
     * @return playerObj.getName() String
     * @see 
     */
	
	public String getPlayerName(int clientId) {
		
		PlayerObj playerObj = players.stream()
				.filter(p -> p.getClientId() == clientId)
				.findFirst()
				.orElse(null);
		
		if (null != playerObj) {
			return playerObj.getName();
		} else {
			
			return null;
		}
		
	}
	
    /**
     * Returns player object belonging to clientId.
     * 
     * @param clientId	The clientId of the target
     * @return Player object PlayerObj
     * @see 
     */
	
	public PlayerObj getPlayerObj(int clientId) {
		
		PlayerObj result = players.stream()
				.filter(p -> p.getClientId() == clientId)
				.findFirst()
				.orElse(null);
		
		return result;
		
		
	}
	
	public List<PlayerObj> getListOfAllPlayers() {
		
		return this.players;
	}
	
    /**
     * Returns a list of all players BUT the provided clientId's.
     * 
     * @param clientId	The clientId of the target
     * @return List<PlayerObj>
     * @see 
     */
	
	public List<PlayerObj> getListOfAllOtherPlayers(int clientId) {
		
		List<PlayerObj> allOtherPlayers = new ArrayList<>();
		this.getListOfAllPlayers().forEach(
	            (pObj) -> {
	            if (pObj.getClientId() != clientId) {
	            	allOtherPlayers.add(pObj);
	            }
	        }
	    );
		
		return allOtherPlayers;
	}
	
	// for SETTINGS cmd
	public void setColourOf(int clientId, String settingColour) {
		// Use function to get the object and set it.
		getPlayerObj(clientId).setSettingColour(settingColour);
	}
	
	public void setBoardSizeOf(int clientId, String settingBoardSize) {
		// Use function to get the object and set it.
		getPlayerObj(clientId).setSettingBoardSize(settingBoardSize);
	}
	
	// for SETTINGS cmd
	public String getColourOf(int clientId) {
		// Use function to get the object and set it.
		return getPlayerObj(clientId).getSettingColour();
	}
	
	public String getBoardSizeOf(int clientId) {
		// Use function to get the object and set it.
		return getPlayerObj(clientId).getSettingBoardSize();
	}
	
	public void removePlayer(int findThisId) {
		
		players.removeIf(i -> {
		    return i.getClientId() == findThisId;
		});
	}

}
