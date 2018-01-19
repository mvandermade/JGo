package serverModel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ConnectionToServerObj {
	
	private int clientId;
	private InputStream inputStream;
	private Queue<ToServerPacket> toServerQueue;
	
	public ConnectionToServerObj(Integer clientId, InputStream inputStream,
			Queue<ToServerPacket> toServerQueue) {
		// TODO Auto-generated constructor stub
		
		this.clientId = clientId;
		this.inputStream = inputStream;
		this.toServerQueue = toServerQueue;
		
		grabInput();
		
	}

	private void grabInput() {
		// TODO Auto-generated method stub
		
		try (       
                BufferedReader in = new BufferedReader(new InputStreamReader(this.inputStream));
            ) {
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                	
                	// Add packet to the queue if a \n is seen.
                	toServerQueue.add(new ToServerPacket(this.clientId, inputLine));
                }
                
            } catch (IOException e) {
                System.out.print("Exception caught when trying to listen to clientId: ");
                System.out.println(this.clientId);
                System.out.println(e.getMessage());
            }
		
	}
	
	public int getClientId() {
		return clientId;
		
	}
	
	

}
