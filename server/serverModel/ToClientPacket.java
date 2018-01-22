package serverModel;

public class ToClientPacket {
	
	private String outputLine;
	private int clientId;
	private long startTime;

	public ToClientPacket(int clientId, String outputLine) {
		
		this.setOutputLine(outputLine);
		this.setClientId(clientId);
		this.setStartTime(System.nanoTime());
		
		//System.out.print("creating transmission packet...");
		//System.out.println(this.stringify());
	}

	private void setClientId(int clientId) {
		// TODO Auto-generated method stub
		this.clientId = clientId;
	}

	public String getOutputLine() {
		return outputLine;
	}
	
	public int getClientId() {
		return clientId;
	}

	public void setOutputLine(String outputLine) {
		this.outputLine = outputLine;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	
	public String stringify() {
		return "TO" + this.clientId +") : " + this.outputLine + " AT:" + this.startTime;
	}

}