package sat.radio.engine.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import sat.radio.RadioSocket;
import sat.radio.RadioSocketDirect;

public class RadioServerTCPEngine extends RadioServerEngine {
	private int port;
	private InetAddress iface = null;
	
	private Thread serverThread;
	
	public RadioServerTCPEngine(int port) {
		this.port = port;
	}
	
	public RadioServerTCPEngine(int port, InetAddress iface) {
		this(port);
		this.iface = iface;
	}
	
	public void init(RadioServerEngineDelegate delegate) throws IOException {
		setDelegate(delegate);
		
		final ServerSocket server = new ServerSocket(port, 50, iface);
		
		serverThread = new Thread() {
			public void run() {
				while(true) {
					try {
						Socket client = server.accept();
						
						RadioSocket socket = new RadioSocketDirect(
								client.getInputStream(),
								client.getOutputStream()
						);
						
						getDelegate().onNewConnection(socket);
					} catch(IOException e) {
						// Ignore bad clients !
					}
				}
			}
		};
		
		serverThread.start();
	}
}