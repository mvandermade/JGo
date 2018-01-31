package clientView;

public class ClientTextInputPacket {

	private long startTime;
	private String inputLine;
	
    /**
     * The object is a data storage of an string of data, a timestamp made on
     * creation on the object and the clientId. 
     * It is used to save messages that were typed in the console and delimited with \n
     * 
     * @param	inputLine	The output that typed in the console.
     * @return	void
     */
	
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

