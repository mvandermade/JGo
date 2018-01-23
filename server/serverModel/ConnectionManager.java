package serverModel;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

public class ConnectionManager {
	
	final List<ConnectedClientObj> clients = new ArrayList<>();
	private Integer clientId = 0;
	
	// These objects can be sent by any client to the server
	final Queue<ToServerPacket> toServerQueue = new ConcurrentLinkedQueue<ToServerPacket>();
	final Queue<ToClientPacket> toClientTxQueue = new ConcurrentLinkedQueue<ToClientPacket>();
	private GameManager gameMan;
	private PlayerManager playMan;

	public ConnectionManager(ServerSocket serverSocket, GameManager gameMan, PlayerManager playMan) {
		// TODO Auto-generated constructor stub
		
		this.gameMan = gameMan;
		this.playMan = playMan;
	}
	
	
	public void addNewClient(Socket skt) throws IOException {
		
		clientId = clientId + 1;
		
		System.out.println("ConnectionManager: NEW client " + clientId);
		
		// Booting an input thread after connection is noticed.
		clients.add(new ConnectedClientObj(clientId, skt));
		
		addToClientTxQueue(new ToClientPacket(clientId, "OTHER", "Hi, je unieke nummer is : " + clientId + "...Begin (eenmalig) met NAME$jeNaam..."));
		
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
		
		// here should be a internal tag for the server. (ERR or something)
		
		toServerQueue.add(new ToServerPacket(findThisId, "INTERNAL ERROR: Observed broken connection, removed"));
		clients.removeIf( i -> {
		      return i.getClientId() == findThisId;//No return statement will break compilation
		    });
		
		playMan.removePlayer(findThisId);
		
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
	
	public void addToClientTxQueue(ToClientPacket toClientPacket) {
		
		this.toClientTxQueue.add(toClientPacket);
		
	}
	
	public void transmitAllToClientQueue() {
		
		List<ToClientPacket> localPolledQueue = new ArrayList<ToClientPacket>();
		Boolean done = false;
		
		while(!done) {
			
			//System.out.print("poll");
			
			ToClientPacket polledObject = this.toClientTxQueue.poll();
			
			//System.out.print(polledObject);
			
			if (polledObject != null) {
				
				localPolledQueue.add(polledObject);
				
			} else {
				
				done = true;
			}
		}
		
		gameMan.getTxQueue().forEach(
				gameTxQueueObj -> {localPolledQueue.add(gameTxQueueObj);}
				);
		
		// And clear it
		gameMan.clearTxQueue();
		
		List<ToClientPacket> transmissionQueue = localPolledQueue.stream()
				.sorted((f1, f2) -> Long.compare(f1.getStartTime(), f2.getStartTime())).
                collect(Collectors.toList());
		
		//System.out.print(".");
		
		transmissionQueue.forEach((Tx)->{
			// Here an object is made ToClientObject
			System.out.print(" |..."+"Server>" + Tx.getClientId()+"): " + Tx.getOutputLine());
			this.transmitToClient(Tx.getClientId(), Tx.getOutputLine());
		});
		
	}
	

}
