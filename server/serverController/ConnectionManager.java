package serverController;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

import serverModel.ConnectedClientObj;
import serverView.ConnectionToServerObj;
import serverView.ToClientPacket;
import serverView.ToServerPacket;

public class ConnectionManager {
	
	final List<ConnectedClientObj> clients = new ArrayList<>();
	private Integer clientId = 0;
	
	// These objects can be sent by any client to the server
	private final Queue<ToServerPacket> toServerQueue =
			new ConcurrentLinkedQueue<ToServerPacket>();
	private final Queue<ToClientPacket> toClientTxQueue =
			new ConcurrentLinkedQueue<ToClientPacket>();
	private GameManager gameMan;
	private PlayerManager playMan;
	
    /**
     * The ConnectionManager has as job to manage all socket connections.
     * It also enables socket connection management by being able to
     * remove or add connections.
     * 
     * The connectionmanager has acces to the instances of gameMan and playMan
     * 
     * @param	port	integer number of the port to open the socket listner service on.
     * @return	void
     */

	public ConnectionManager(ServerSocket serverSocket, GameManager gameMan,
			PlayerManager playMan) {
		
		this.gameMan = gameMan;
		this.playMan = playMan;
	}
	
	
    /**
     * This method adds a new client to the list and gives it an unique Id
     * The client is then assigned an ConnectionToServerObj in it's own thread.
     * 
     * @param	skt		the .accept() 'ed socket
     * @return	void
     */
	
	public void addNewClient(Socket skt) throws IOException {
		
		clientId = clientId + 1;
		
		System.out.println("ConnectionManager: NEW client " + clientId);
		
		
		// Booting an input thread after connection is noticed.
		clients.add(new ConnectedClientObj(clientId, skt));
		
		addToClientTxQueue(new ToClientPacket(clientId,
				"OTHER",
				"Hi, Whats your name? use: NAME$YOURNAME to continue."));
		addToClientTxQueue(new ToClientPacket(clientId,
				"CMDHINTS",
				"CHAT, NAME$<name>, LOBBY, MOVE$r_c, MOVE$PASS, REQUESTGAME, SETTINGS, QUIT, "));
		
		(new Thread() {
			public void run() {
			// do something 
				ConnectionToServerObj threadedConn = null;
				while (true) {
					
					try {
						threadedConn = new ConnectionToServerObj(clientId,
								skt.getInputStream(),
								toServerQueue);
						
					} catch (IOException e) {
						System.out.println("Socket ERROR"
								+ threadedConn.getClientId()
								+ "kicking...");
						
						removeClientById(threadedConn.getClientId());
						System.out.println("Kicked");
						break;
					}
				}
			}
		}).start(); // Boot the thread
	}
	
    /**
     * This method searches the clients list for a client object matching the provided id.
     * 
     * @param	findThisClientId	The clientId you wish to obtain an object for.
     * @return	Optional<ConnectedClientObj>
     */
	
	public Optional<ConnectedClientObj> getClientById(int findThisClientId) {
		
		return this.clients.stream()
	            .filter(connectedClientObj -> connectedClientObj.getClientId() == findThisClientId)
	            .findAny();
		
	}
	
	
    /**
     * This method searches the clients list for a client object matching the provided id.
     * Then it will delete:
     * - Quit the game the player is in
     * - Remove the client
     * - Clear the request queue from the clientId
     * - remove the associated player with the clientId.
     * 
     * @param	findThisClientId	The clientId you wish to obtain an object for.
     * @return	void
     */
	public void removeClientById(int findThisId) {
		
		// here should be a internal tag for the server. (ERR or something)
		gameMan.errorQuit2PGameFor(findThisId);
		System.out.println("Quit: Broken connection" + findThisId);
		clients.removeIf(i -> {
		    return i.getClientId() == findThisId;
		});
		
		// Flush the user from a queue (if present)
		gameMan.removeFromRequestQueue(findThisId);
		
		// Kick out of the player manager
		playMan.removePlayer(findThisId);
		
		// <garbage collection done>
		
		
	}
	
	public int getLatestClientId() {
		
		return this.clientId;
	}
	
	public Queue<ToServerPacket> getToServerQueue() {
		
		return this.toServerQueue;
		
	}

	public List<ConnectedClientObj> getClients() {
		return clients;
	}
	
	public void addToClientTxQueue(ToClientPacket toClientPacket) {
		
		this.toClientTxQueue.add(toClientPacket);
		
	}
	
    /**
     * Transmission flush trigger.
     * Pulls the enqueued messages from the global queue, saves them
     * internally and sorts FiFo.
     * Then the message is sent to clients using transmitToClient
     * 
     * @param 
     * @see transmitToClient
     */
	
	public void transmitAllToClientQueue() {
		
		// Grab all concurrent list objects to a 'local' list over which is iterated.
		// Sort FiFo (First in First out)
		List<ToClientPacket> localPolledQueue = new ArrayList<ToClientPacket>();
		Boolean done = false;
		
		// Because polling has no fixed endpoint
		while (!done) {
			
			ToClientPacket polledObject = this.toClientTxQueue.poll();
			
			if (polledObject != null) {
				
				localPolledQueue.add(polledObject);
				
			} else {
				
				done = true;
			}
		}
		
		gameMan.getTxQueue().forEach(
				gameTxQueueObj -> {
				localPolledQueue.add(gameTxQueueObj);
			}
		);
		
		// And clear the concurrent list
		gameMan.clearTxQueue();
		
		List<ToClientPacket> transmissionQueue = localPolledQueue.stream()
				.sorted((f1, f2) -> Long.compare(f1.getStartTime(), f2.getStartTime())).
                collect(Collectors.toList());
		
		transmissionQueue.forEach((toClientPacket) -> {
			// Here an object is made ToClientObject
			System.out.println(" |..." + "Server>" 
					+ toClientPacket.getClientId() + "): " + toClientPacket.getOutputLine());
			this.transmitToClient(toClientPacket.getClientId(), toClientPacket.getOutputLine());
		});
		
	}
	
    /**
     * Transmission trigger of a message. Will trigger the socket output to the client
     * to flush the inputted string.
     * 
     * @param clientIdtarget	The clientId of the target
     * @param msg				The message string to deliver to the client.
     * @see transmitAllToClientQueue
     */
	public void transmitToClient(int clientIdtarget, String msg) {
		
		//ConnectedClientObj
		getClientById(clientIdtarget).ifPresent(connectedClientObj -> {
			try {
				connectedClientObj.sendStrToClientSkt(msg);
			} catch (IOException e) {
				// Break connection with client, and remove cleanly from any queue
				removeClientById(clientIdtarget);
			}
		});
	}
	

}
