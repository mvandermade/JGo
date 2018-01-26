package client;

public class ClientTextInputPacket {

	private long startTime;
	private String inputLine;
	
	public ClientTextInputPacket(String inputLine) {
		
	this.setStartTime(System.nanoTime());
	this.inputLine = inputLine;
	
	}
	
	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public String getInputLine() {
		// TODO Auto-generated method stub
		return this.inputLine;
	}

}

