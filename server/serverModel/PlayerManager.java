package serverModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PlayerManager {
	
	final List<PlayerObj> activePlayers = new ArrayList<>();
	
	public PlayerManager() {
		
		
	}
	
	public void addPlayer(int clientId, String name) {
				
		 boolean pAlreadyExists = activePlayers.stream()
		            .anyMatch(p -> p.getClientId()==clientId);
			
		if (!pAlreadyExists) {
			activePlayers.add(new PlayerObj(clientId, name));
		}
		
	}
	
	public String GetPlayerName(int clientId) {
		
		PlayerObj result = activePlayers.stream()
				.filter(p -> p.getClientId()==clientId)
				.findFirst()
				.orElse(null);
		
		return result.getName();
		
	}
	
	public void removePlayer(int findThisId) {
		
		activePlayers.removeIf( i -> {
		      return i.getClientId() == findThisId;//No return statement will break compilation
		    });
	}

}
