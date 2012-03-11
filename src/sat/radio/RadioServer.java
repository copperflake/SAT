package sat.radio;

import java.net.InetAddress;

import sat.radio.engine.ServerRadioEngine;

public class RadioServer extends Radio {
	public RadioServer() {
		super();
	}
	
	public void listen(ServerRadioEngine engine) {
		engine.init();
	}
}
