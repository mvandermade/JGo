package serverView;

import server.Server;

public class ToClientPacket {
	
	private String outputLine;
	private String serverCMD;
	private int clientId;
	private long startTime;
	private String servDELIMITER1 = Server.getDELIMITER1();

    /**
     * The object is a data storage of an string of data, a timestamp made on
     * creation on the object and the clientId. 
     * It is used to save messages that are outbound towards a client.
     * 
     * @param	clientId	The clientId number this package should go to
     * @param	serverCMD	The string containing the type of command the server sends
     * @param	outputLine	The string containing the command payload.
     * @return	void
     * 
     * 
     *
     */
	public ToClientPacket(int clientId, String serverCMD, String outputLine) {
		
		
		this.setOutputLine(serverCMD + servDELIMITER1 + outputLine);
		this.setClientId(clientId);
		this.setStartTime(System.nanoTime());
		
	}

	private void setClientId(int clientId) {
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
		return "TO" + this.clientId + ")"
				+ this.serverCMD + this.servDELIMITER1
				+ this.outputLine + " AT:" + this.startTime;
	}

	public String getServerCMD() {
		return serverCMD;
	}

	public void setServerCMD(String serverCMD) {
		this.serverCMD = serverCMD;
	}

}