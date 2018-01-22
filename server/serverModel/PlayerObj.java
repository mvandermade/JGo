package serverModel;

public class PlayerObj {

	private int clientId;
	private String name;

	public PlayerObj(int clientId, String name) {
		// TODO Auto-generated constructor stub
		// TCP id, Name
		this.clientId = clientId;
		this.setName(name);
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

}
