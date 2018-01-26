package client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

public class Client {
	
	
	// TUI
	final Queue<ClientTextInputPacket> clientTextInputQueue = new ConcurrentLinkedQueue<ClientTextInputPacket>();
	
	// Outbox TCP
	final Queue<ClientOutToServerPacket> clientOutToServerQueue = new ConcurrentLinkedQueue<ClientOutToServerPacket>();
	// Inbox TCP
	final Queue<ServerInToClientPacket> serverInToClientQueue = new ConcurrentLinkedQueue<ServerInToClientPacket>();
	
	private Socket skt = null;
	
	private BufferedWriter bufferedWriterToServer = null;

	
	private int pollQueueTime;

	public Client(String servername, int port) {
		// TODO Auto-generated constructor stub
		
		// 500ms mainloop delay
		this.pollQueueTime = 500;
		
		try {
			skt = new Socket(servername, port);
			bufferedWriterToServer = new BufferedWriter( new OutputStreamWriter( skt.getOutputStream()) );
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		// Use a part of the server thread
		(new Thread() {
			public void run() {
			// do something 
				HandleClientInbox();
			}
		}).start(); // Boot the thread
		
		// Handle console 
		(new Thread() {
			public void run() {
			// do something 
				HandleTUI(new BufferedReader(new InputStreamReader(System.in)));
			}
		}).start(); // Boot the thread
		
		
		// Start the running part
		runClientLoop();
		
	}
	
	private void runClientLoop() {
		// TODO Auto-generated method stub
		while (true) {
			try {
				Thread.sleep(pollQueueTime);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			// 1. Since the 1 'inbox' thread is running asynchrone, the poll method is used.
			// Timestamps are then used to sort
			
			// Writes to serverInToClientQueue
			clientPullAndProcessInbox();
			
			//2 See the input scanner for new messages and process them
			clientPullAndProcessTUI();
			
			//3. Packets are sent out async.
			FlushClientOutbox();
			
		} // end while true client loop
		
	}

	private void clientPullAndProcessInbox() {
		// TODO Auto-generated method stub
		List<ServerInToClientPacket> localPolledQueue = new ArrayList<ServerInToClientPacket>();
		Boolean done = false;
		
		while(!done) {
			ServerInToClientPacket polledObject = serverInToClientQueue.poll();
			// null is the native response for 'no queue items left'
			if (polledObject != null) {
				localPolledQueue.add(polledObject);
			} else {
				done = true;
			}
		}
		
		// Synchronize using timestamping. So output can be done in order of array.
		List<ServerInToClientPacket> servletQueue = localPolledQueue.stream()
				.sorted((f1, f2) -> Long.compare(f1.getStartTime(), f2.getStartTime())).
                collect(Collectors.toList());
		
		// Pass all to clientResponderServlet
		servletQueue.forEach((c)->{
			//System.out.println("GOT TCP+++++++"); 
			clientResponderServlet(c); 
			//System.out.println("END TCP-------");
			
		});
		
	}
	
	private void clientPullAndProcessTUI() {
		// TODO Auto-generated method stub
		// Queue<ClientTextInputPacket> clientTextInputPacket
		List<ClientTextInputPacket> localPolledQueue = new ArrayList<ClientTextInputPacket>();
		Boolean done = false;
		
		while(!done) {
			ClientTextInputPacket polledObject = clientTextInputQueue.poll();
			// null is the native response for 'no queue items left'
			if (polledObject != null) {
				localPolledQueue.add(polledObject);
			} else {
				done = true;
			}
		}
		
		// Synchronize using timestamping. So output can be done in order of array.
		List<ClientTextInputPacket> textQueue = localPolledQueue.stream()
				.sorted((f1, f2) -> Long.compare(f1.getStartTime(), f2.getStartTime())).
                collect(Collectors.toList());
		
		// Pass all to clientResponderServlet
		textQueue.forEach((c)->{
			//System.out.println("GOT TEXT..."); 
			//System.out.println(c.getInputLine());
			clientTUIResponder(c); 
			//System.out.println("END TEXT...");
			
		});
		
	}

	private void clientResponderServlet(ServerInToClientPacket c) {
		// TODO Auto-generated method stub
		
		// Intelligence happens here
		// Talk back to Command line
		System.out.println(c.getInputLine());
		
	}
	
	private void clientTUIResponder(ClientTextInputPacket c) {
		// TODO Auto-generated method stub
		
		// Filtering of commands
		// Things like that
		
		// Then afterwards post it
		
		// Intelligence happens here
		
		// Put into sender queue
		clientOutToServerQueue.add(new ClientOutToServerPacket(c.getInputLine()));
		// Second ✓
		System.out.print ("✓");
		
	}

	private void FlushClientOutbox() {
		// TODO Auto-generated method stub
		
		// Queue<ClientTextInputPacket> clientTextInputPacket
		List<ClientOutToServerPacket> localPolledQueue = new ArrayList<ClientOutToServerPacket>();
		Boolean done = false;
		
		while(!done) {
			ClientOutToServerPacket polledObject = clientOutToServerQueue.poll();
			// null is the native response for 'no queue items left'
			if (polledObject != null) {
				localPolledQueue.add(polledObject);
			} else {
				done = true;
			}
		}
		
		// Synchronize using timestamping. So output can be done in order of array.
		List<ClientOutToServerPacket> textQueue = localPolledQueue.stream()
				.sorted((f1, f2) -> Long.compare(f1.getStartTime(), f2.getStartTime())).
                collect(Collectors.toList());
		
		// Pass all to clientResponderServlet
		
		if (textQueue.size() > 0) {
			textQueue.forEach((c)->{
				//System.out.println("SENDING OUT..."); 
				// Write line
				try {
					bufferedWriterToServer.write(c.getInputLine());
					// Protocol
					bufferedWriterToServer.newLine();
					// Send
					bufferedWriterToServer.flush();
					
					
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//System.out.println("END OUT.......");
				
			});
			
			System.out.println("↗");
		}
		

		
	}

	// Method to receive messages
	private void HandleClientInbox() {
		
		// Respond to the server input
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
                	
                	serverInToClientQueue.add(new ServerInToClientPacket(inputLine));
                	
                }
                
            } catch (IOException e) {
            	e.printStackTrace();
            }
		
	}
	
	private void HandleTUI(BufferedReader bufferedReaderTextInput) {
		// TODO Auto-generated method stub
		while (true) {
			try {
				// Each line is seen as a command, and put into the queue
				
				clientTextInputQueue.add(new ClientTextInputPacket(bufferedReaderTextInput.readLine()));
				// First ✓
				System.out.print("✓");
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
	}
	
	

}
