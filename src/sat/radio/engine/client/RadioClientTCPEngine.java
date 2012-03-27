package sat.radio.engine.client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class RadioClientTCPEngine extends RadioClientEngine {
	private InetAddress host;
	private int port;
	private Socket socket;

	public RadioClientTCPEngine(InetAddress host, int port) {
		this.host = host;
		this.port = port;
	}

	public void init(RadioClientEngineDelegate delegate) throws IOException {
		socket = new Socket(host, port);
	}
}
