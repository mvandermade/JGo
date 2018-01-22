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
	
	public String GetPlayerName(int clientId) {
		
		PlayerObj result = Players.stream()
				.filter(p -> p.getClientId()==clientId)
				.findFirst()
				.orElse(null);
		
		return result.getName();
		
	}
	
	public List<PlayerObj> GetListOfAllPlayers() {
		
		return this.Players;
	}
	
	public List<PlayerObj> GetListOfAllOtherPlayers(int clientId) {
		
		List<PlayerObj> allOtherPlayers = new ArrayList<>();
		this.GetListOfAllPlayers().forEach(
	            (pObj) -> {
	                if (pObj.getClientId() != clientId) {
	                	allOtherPlayers.add(pObj);
	                }
	            }
	    );
		
		return allOtherPlayers;
	}
	
	public void removePlayer(int findThisId) {
		
		Players.removeIf( i -> {
		      return i.getClientId() == findThisId;//No return statement will break compilation
		    });
	}

}
