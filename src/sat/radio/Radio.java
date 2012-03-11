package sat.radio;

import sat.file.*;
import sat.radio.message.*;

public abstract class Radio {
	public Radio() {
		
	}
	
	public void file(String path, String dest) {
		DataFile file = new DataFile(path);
	}
	
	protected void send(Message msg, String dest) {
		
	}
}
