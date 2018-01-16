package serverView;

import java.io.IOException;
import java.net.ServerSocket;
import java.time.LocalDateTime;
import java.util.Date;

import NetTCP.InboundHandler;

public class WaitingRoom {

	private Boolean waitingForPlayer = true;
	private InboundHandler[] InboundPlayers = new InboundHandler[2];
	
	public WaitingRoom(final ServerSocket serverSocket, final int clientCounter) {
		
		int successConnects = 0;
		System.out.println("WAITINGROOM: Hello Waiting for P1 & P2");
		while (waitingForPlayer) {
			
			try {
				
				InboundPlayers[successConnects] = new InboundHandler(serverSocket.accept(), clientCounter);
				successConnects = successConnects + 1;
				
				// Need two players
				if (successConnects == 2) {
					
					waitingForPlayer = false;
					System.out.println("WAITINGROOM: LAUNCHING GAME");
					
				} else {
					
					System.out.println("WAITINGROOM: P1 present waiting for P2 to arrive");
					
				}
			
			} catch(IOException e) {
				
				// Trying again
				e.printStackTrace();
			}
		}
		
		System.out.println("waitingroom 1P, 2P present launching game");

	}
	
	public InboundHandler[] getWaitingPlayers() {
		
		return this.InboundPlayers;
	}

}
