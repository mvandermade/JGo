package serverModel;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ConnectionManager {
	
	final List<ConnectedClientObj> clients = new ArrayList<>();
	private Integer clientId = 0;
	
	// These objects can be sent by any client to the server
	final Queue<ToServerPacket> toServerQueue = new ConcurrentLinkedQueue<ToServerPacket>();
	final Queue<ToClientPacket> toClientQueue = new ConcurrentLinkedQueue<ToClientPacket>();

	public ConnectionManager(ServerSocket serverSocket) {
		// TODO Auto-generated constructor stub
	}
	
	public void addNewClient(Socket skt) throws IOException {
		
		clientId = clientId + 1;
		
		System.out.println("ConnectionManager: NEW client " + clientId);
		
		// Booting an input thread after connection is noticed.
		clients.add(new ConnectedClientObj(clientId, skt));
		
		transmitToClient(clientId, "...............................................it starts with hello world...............................................\nHi, je nummer is : " + clientId + "\nTyp iets en je bericht wordt rondgestuurd!...");
		
		(new Thread() {
			public void run() {
			// do something 
				ConnectionToServerObj threadedConn = null;
				while (true) {
					
					try {
						
						threadedConn = new ConnectionToServerObj(clientId, skt.getInputStream(), toServerQueue);
						
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						
						removeClientById(threadedConn.getClientId());
						break;
					}
				}
			}
		}).start(); // Boot the thread
		
		//
		
		
	}
	
	public Optional<ConnectedClientObj> getClientById(int findThisId) {
		
		return (this.clients.stream()
	            .filter(a -> a.getClientId() == findThisId)
	            .findAny()
	            );
		
	}
	
	public void removeClientById(int findThisId) {
		
		toServerQueue.add(new ToServerPacket(findThisId, "Observed broken connection, removed\n"));
		clients.removeIf( i -> {
		      return i.getClientId() == findThisId;//No return statement will break compilation
		    });
		
	}
	
	public int getLatestClientId() {
		
		return this.clientId;
	}
	
	public Queue<ToServerPacket> getToServerQueue() {
		
		 return this.toServerQueue;
		
	}
	
	public void transmitToClient(int clientId, String msg) {
		
		//ConnectedClientObj
		getClientById(clientId).ifPresent(connectedClientObj -> connectedClientObj.sendStrToClientSkt(msg));
		
		// Transmission starting
	}

	public List<ConnectedClientObj> getClients() {
		return clients;
	}
	

}
