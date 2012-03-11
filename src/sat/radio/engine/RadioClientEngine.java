package sat.radio.engine;

import java.io.IOException;

import sat.radio.RadioClient;

public abstract class RadioClientEngine extends RadioEngine {
	protected RadioClient radioClient;
	
	public void init(RadioClient radioClient) throws IOException {
		this.radioClient = radioClient;
	}
}
