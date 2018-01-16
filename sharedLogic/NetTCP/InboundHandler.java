package NetTCP;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class InboundHandler implements Runnable {

	private final Socket inS;
	private final int launchCounter;
	private OutputStream outStream;
	private InputStream inStream;
	
	// Socket (open) and launchcounter (id number)
	public InboundHandler(Socket inS, int launchCounter) throws IOException {
		
		this.inS = inS;
		this.launchCounter = launchCounter;
		this.inStream = inS.getInputStream();
		this.outStream = inS.getOutputStream();
		
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		
	}
	public Socket getinS() {
		return inS;
	}
	public int getLaunchCounter() {
		return launchCounter;
	}

}
