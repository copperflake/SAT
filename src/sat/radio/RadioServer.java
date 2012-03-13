package sat.radio;

import java.io.IOException;

import sat.radio.engine.server.RadioServerEngine;
import sat.radio.engine.server.RadioServerEngineDelegate;

public class RadioServer extends Radio implements RadioServerEngineDelegate {
	RadioServerDelegate delegate;
	RadioServerEngine engine;
	
	public RadioServer(RadioServerDelegate delegate) {
		this.delegate = delegate;
	}
	
	public void listen(RadioServerEngine engine) throws IOException {
		this.engine = engine;
		engine.init(this);
	}

	public void onNewConnection(RadioSocket socket) {
		System.out.println("New Connection");
	}
}
