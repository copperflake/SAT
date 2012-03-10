package sat.com;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Radio {
	
	public Radio() {
		
	}
	
	public void hello(String dest) {
		this.send(new MessageHello(), dest);
	}
	
	public void sendFile(String path, String dest) {
		DataFile file = new DataFile(path);
	}
	
	private void send(Message msg, String dest) throws IOException {
		try {
			//PrintWriter outStream = new PrintWriter(new FileWriter(dest, true));
			outStream.println(msg);
			outStream.close();
		}
		catch(IOException e) {
		}
		return;
	}
	
	public void listen();
}
