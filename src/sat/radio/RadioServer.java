package sat.radio;

import java.io.IOException;

import sat.radio.engine.RadioServerEngine;

public class RadioServer extends Radio {
	public RadioServer() {
		super();
	}
	
	public void listen(RadioServerEngine engine) throws IOException {
		engine.init(this);
	}
}
