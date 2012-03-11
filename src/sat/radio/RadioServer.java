package sat.radio;

import java.io.IOException;

import sat.radio.engine.RadioServerEngine;

public class RadioServer extends Radio {
	RadioServerDelegate delegate;
	RadioServerEngine engine;
	
	public RadioServer(RadioServerDelegate delegate) {
		this.delegate = delegate;
	}
	
	public void listen(RadioServerEngine engine) throws IOException {
		this.engine = engine;
		engine.init(this);
	}
}
