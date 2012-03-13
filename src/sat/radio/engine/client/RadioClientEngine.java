package sat.radio.engine.client;

import java.io.IOException;

import sat.radio.RadioClient;
import sat.radio.engine.RadioEngine;

public abstract class RadioClientEngine extends RadioEngine {
	protected RadioClient radioClient;
	
	public void init(RadioClient radioClient) throws IOException {
		this.radioClient = radioClient;
	}
}
