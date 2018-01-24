package serverModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PlayerManager {
	
	final List<PlayerObj> Players = new ArrayList<>();
	
	public PlayerManager() {
		
		
	}
	
	public void addPlayer(int clientId, String name) {
				
		 boolean pAlreadyExists = Players.stream()
		            .anyMatch(p -> p.getClientId()==clientId);
			
		if (!pAlreadyExists) {
			Players.add(new PlayerObj(clientId, name));
		}
		
	}
	
	public String getPlayerName(int clientId) {
		
		PlayerObj playerObj = Players.stream()
				.filter(p -> p.getClientId()==clientId)
				.findFirst()
				.orElse(null);
		
		if (null != playerObj) {
			return playerObj.getName();
		} else {
			
			return null;
		}
		
	}
	
	public PlayerObj getPlayerObj(int clientId) {
		
		PlayerObj result = Players.stream()
				.filter(p -> p.getClientId()==clientId)
				.findFirst()
				.orElse(null);
		
		return result;
		
		
	}
	
	public List<PlayerObj> getListOfAllPlayers() {
		
		return this.Players;
	}
	
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
		
		Players.removeIf( i -> {
		      return i.getClientId() == findThisId;
		    });
	}

}
