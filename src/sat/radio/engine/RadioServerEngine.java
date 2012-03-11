package sat.radio.engine;

import java.io.IOException;

import sat.radio.RadioServer;

public abstract class RadioServerEngine extends RadioEngine {
	protected RadioServer radioServer;
	
	public void init(RadioServer radioServer) throws IOException {
		this.radioServer = radioServer;
	}
}
