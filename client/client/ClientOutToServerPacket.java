package client;

public class ClientOutToServerPacket {
	
	private long startTime;
	private String inputLine;

	public ClientOutToServerPacket(String inputLine) {
		
		this.setStartTime(System.nanoTime());
		this.inputLine = inputLine;
		
		// TODO Auto-generated constructor stub
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
