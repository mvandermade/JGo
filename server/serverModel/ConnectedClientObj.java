package serverModel;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class ConnectedClientObj {
	
	private Socket skt;
	private int clientId;
	BufferedWriter bufWToServer = null;

	public ConnectedClientObj(int clientId, Socket skt) {
		
		this.setClientId(clientId);
		this.skt = skt;
		try {
			bufWToServer = new BufferedWriter( new OutputStreamWriter( skt.getOutputStream() ) );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Socket getActiveSocket() {
		
		return this.skt;
	}

	public int getClientId() {
		return clientId;
	}

	public void setClientId(int clientId) {
		this.clientId = clientId;
	}
	
	public void sendStrToClientSkt(String msg) {
		
		try {
			// Write line
			bufWToServer.write(msg);
			// Protocol
			bufWToServer.newLine();
			// Send
			bufWToServer.flush();
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
