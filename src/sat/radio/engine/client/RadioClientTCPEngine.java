package sat.radio.engine.client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import sat.radio.socket.RadioSocket;
import sat.radio.socket.RadioSocketDirect;

public class RadioClientTCPEngine extends RadioClientEngine {
	private InetAddress host;
	private int port;
	private RadioSocket socket;

	public RadioClientTCPEngine(InetAddress host, int port) {
		this.host = host;
		this.port = port;
	}

	public RadioSocket init(RadioClientEngineDelegate delegate) throws IOException {
		Socket s = new Socket(host, port);
		socket = new RadioSocketDirect(s.getInputStream(), s.getOutputStream());
		return socket;
	}
}
