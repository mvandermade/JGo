package serverModel;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ConnectedClientObj {
	
	private Socket skt;
	private int clientId;
	BufferedWriter bufWToServer = null;
	
    /**
     * The connected player object instantiates a buffered writer
     * The handles of the object can then be used to write data over
     * this buffered writer.
     * 
     * @param clientId	The clientId of the target
     * @param skt		socket object "socket.accept()'ed"
     * @see 
     */

	public ConnectedClientObj(int clientId, Socket skt) {
		
		this.setClientId(clientId);
		this.skt = skt;
		try {
			bufWToServer = new BufferedWriter(new OutputStreamWriter(skt.getOutputStream()));
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
	
	
    /**
     * Send a string to a user via the protocol.
     * \n newline delimited.
     * flush -> send immediately.
     * 
     * @param msg	String of the message to send to this objects attached buffer
     * @see 
     */
	public void sendStrToClientSkt(String msg) throws IOException {			
			
		// Write line
		bufWToServer.write(msg);
		// Protocol
		bufWToServer.newLine();
		// Send
		bufWToServer.flush();
		
		
	}

}
