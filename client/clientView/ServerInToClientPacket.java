package clientView;

public class ServerInToClientPacket {

	private long startTime;
	private String inputLine;
	
	
    /**
     * The object is a data storage of an string of data, a timestamp made on
     * creation on the object and the clientId. 
     * It is used to save messages that are inbound towards the client from the server.
     * 
     * @param	inputLine	The output that was sent to the client.
     * @return	void
     */
	public ServerInToClientPacket(String inputLine) {
		
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
