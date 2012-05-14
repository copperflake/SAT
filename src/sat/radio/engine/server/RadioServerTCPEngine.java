package sat.radio.engine.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import sat.radio.socket.RadioSocket;
import sat.radio.socket.RadioSocketDirect;

public class RadioServerTCPEngine extends RadioServerEngine {
	private int port;
	private InetAddress iface = null;

	private Thread serverThread;

	public RadioServerTCPEngine(int port) {
		this(port, null);
	}

	public RadioServerTCPEngine(int port, InetAddress iface) {
		this.port = port;
		this.iface = iface;
	}

	public void init(final RadioServerEngineDelegate delegate) throws IOException {
		this.delegate = delegate;

		final ServerSocket server = new ServerSocket(port, 50, iface);

		serverThread = new Thread() {
			public void run() {
				while(true) {
					try {
						Socket client = server.accept();
						RadioSocket socket = new RadioSocketDirect(client.getInputStream(), client.getOutputStream());
						delegate.onNewConnection(socket);
					}
					catch(IOException e) {
						// Ignore bad clients !
					}
				}
			}
		};

		serverThread.start();
	}
}
