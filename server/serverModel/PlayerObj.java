package serverModel;

public class PlayerObj {

	private int clientId;
	private String name;
	private String settingColour;
	private String settingBoardSize; // use string because of string communication
	private Boolean isInGame;

	public PlayerObj(int clientId, String name) {
		// TODO Auto-generated constructor stub
		// TCP id, Name
		this.clientId = clientId;
		this.setName(name);
		
		// Default is false
		this.setIsInGame(false);
	}

	public int getClientId() {
		return clientId;
	}
	
	// Can only set clientId on construction

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSettingColour() {
		return settingColour;
	}

	public void setSettingColour(String settingColour) {
		this.settingColour = settingColour;
	}

	public String getSettingBoardSize() {
		return settingBoardSize;
	}

	public void setSettingBoardSize(String settingBoardSize) {
		this.settingBoardSize = settingBoardSize;
	}

	public Boolean getIsInGame() {
		return isInGame;
	}

	public void setIsInGame(Boolean isInGame) {
		this.isInGame = isInGame;
	}

}
