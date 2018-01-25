package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;

import serverModel.ToServerPacket;

public class Client {
	
	private Socket skt = null;

	public Client(String servername, int port) {
		// TODO Auto-generated constructor stub
		try {
			skt = new Socket(servername, port);
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Use a part of the server thread
		
		// Auto closing
		try (       
                BufferedReader in = new BufferedReader(new InputStreamReader(skt.getInputStream()));
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
                	
                	ClientServlet(inputLine);
                	
                }
                
            } catch (IOException e) {
            	e.printStackTrace();
            }
	}
	
	void ClientServlet(String inputLine) {
		
		// Respond to the server
		
	}
	
	

}
