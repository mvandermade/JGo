package serverView;

public class ToServerPacket {
	
	private String inputLine;
	private int clientId;
	private long startTime;
	
    /**
     * The object is a data storage of an string of data, a timestamp made on
     * creation on the object and the clientId. 
     * It is used to save messages that are inbound towards the server.
     * 
     * @param	clientId	The clientId number this package should go to
     * @param	inputLine	The output to be sent to the client.
     * @return	void
     * 
     * 
     *
     */
	public ToServerPacket(int clientId, String inputLine) {
		
		this.setInputLine(inputLine);
		this.setClientId(clientId);
		this.setStartTime(System.nanoTime());
		
		//System.out.print("creating inbound packet...");
		//System.out.println(this.stringify());
	}

	private void setClientId(int clientId) {
		// TODO Auto-generated method stub
		this.clientId = clientId;
	}

	public String getInputLine() {
		return inputLine;
	}
	
	public int getClientId() {
		return clientId;
	}

	public void setInputLine(String inputLine) {
		this.inputLine = inputLine;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	
	public String stringify() {
		return this.clientId +") SAYS: " + this.inputLine + " AT:" + this.startTime;
	}

}