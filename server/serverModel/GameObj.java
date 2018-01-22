package serverModel;

public class GameObj {
	
	PlayerObj P1 = null;
	PlayerObj P2 = null;
	
	GameObj(PlayerObj P1) {
		
		this.P1 = P1;
		
	}
	
	public void addP2(PlayerObj P2) {
		
		this.P2 = P2;
		
	}

}
