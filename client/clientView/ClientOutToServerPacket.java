package clientView;

public class ClientOutToServerPacket {
	
	private long startTime;
	private String inputLine;

    /**
     * The object is a data storage of an string of data, a timestamp made on
     * creation on the object and the clientId. 
     * It is used to send messages towards the server later on.
     * 
     * @param	inputLine	The output that needs to be sent.
     * @return	void
     */
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
