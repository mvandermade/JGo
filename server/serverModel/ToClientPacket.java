package serverModel;

import server.Server;

public class ToClientPacket {
	
	private String outputLine;
	private String serverCMD;
	private int clientId;
	private long startTime;
	private String servDELIMITER1 = Server.getDELIMITER1();
	private String servDELIMITER2 = Server.getDELIMITER2();

	public ToClientPacket(int clientId, String serverCMD, String outputLine) {
		
		
		this.setOutputLine(serverCMD+servDELIMITER1+outputLine);
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
		return "TO" + this.clientId +")" + this.serverCMD + this.servDELIMITER1 + this.outputLine + " AT:" + this.startTime;
	}

	public String getServerCMD() {
		return serverCMD;
	}

	public void setServerCMD(String serverCMD) {
		this.serverCMD = serverCMD;
	}

}