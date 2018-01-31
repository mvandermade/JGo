package serverView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Queue;

public class ConnectionToServerObj {
    /**
     * Connection to Server objects are used to keep an client->Server Socket connection 'alive'.
     * It is therefore advisable to call this method from a separate thread.
     * 
     * When an IOException is raised the grabInput() method does not recover, an error message 
     * is printed.
     * An UnsupportedEncodingException is kept silent, the object keeps the connection 'alive' 
     * and reads a new line again.
     * 
     * @param	clientId		integer of the clientId
     * @param	inputStream		an active inputStream to read from 
     * @param 	toServerQueue	a queue that is able to accept .add(<ToServerPacket>)
     * @return	void
     * 
     * @see ConnectionManager.addNewClient()
     */
	
	private int clientId;
	private InputStream inputStream;
	private Queue<ToServerPacket> toServerQueue;
	
	public ConnectionToServerObj(Integer clientId, InputStream inputStream,
			Queue<ToServerPacket> toServerQueue) {		
		this.clientId = clientId;
		this.inputStream = inputStream;
		this.toServerQueue = toServerQueue;
		grabInput();
		
	}

    /**
     * Any data observed flowing into this socket is parsed using a String linereader 
     * (line ends with: \n).
     * Strings are converted to UTF-8.
     * Concurrently done, meaning the queueing is more thread-safe.
     * 
     * When an IOException is raised the object does not recover, an error message is printed.
     * An UnsupportedEncodingException is kept silent, the object keeps the connection 'alive'
     *  and reads a new line again.
     * 
     * @param	guiPacket
     * @return	void
     * 
     * @see initBoardLines();
     */
	
	private void grabInput() {
		
		try (
            BufferedReader in = new BufferedReader(new InputStreamReader(this.inputStream));
        ) {
            String inputLineUTF8;
            String inputLine = null; //UTF16
            while ((inputLineUTF8 = in.readLine()) != null) {
            	
            	try {
            	    // Convert from Unicode to UTF-8
            	    byte[] utf8 = inputLineUTF8.getBytes("UTF-8");

            	    // Convert from UTF-8 to Unicode
            	    inputLine = new String(utf8, "UTF-8");
            	} catch (UnsupportedEncodingException e) {
            	}
            	
            	// Add packet to the queue if a \n is seen.
            	toServerQueue.add(new ToServerPacket(this.clientId, inputLine));
            }
            
        } catch (IOException e) {
            System.out.print("Exception caught when trying to listen to clientId: ");
            System.out.println(this.clientId);
        }
		
	}
	
	public int getClientId() {
		return clientId;
		
	}
	
	

}
