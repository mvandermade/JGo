package server;

public class Server {

	public Server() {
		
		System.out.println("booting... port 8585");
		
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			new Server();
			
		} catch (Exception e) {
			
			System.out.println("Server stopped unexpected...:");
			e.printStackTrace();
			System.out.println("REBOOT MANUALLY...:");
			
		}
		
		
	}
	
}
