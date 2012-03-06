package sat.com;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Radio {
	public void send(Message msg, String dest) throws IOException {
		try {
			PrintWriter outStream = new PrintWriter(new FileWriter(dest, true));
			outStream.println(msg);
			outStream.close();
		}
		catch(IOException e) {
		}
		finally {
			return;
		}
	}
	
	public void listen();
}
