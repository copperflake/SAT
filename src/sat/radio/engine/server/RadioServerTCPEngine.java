package sat.radio.engine.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import sat.radio.socket.RadioSocket;
import sat.radio.socket.RadioSocketDirect;

/**
 * Moteur de serveur radio sur TCP.
 */
public class RadioServerTCPEngine extends RadioServerEngine {
	/**
	 * Le port d'écoute.
	 */
	private int port;

	/**
	 * L'interface d'écoute. (Adresse de bind)
	 */
	private InetAddress iface = null;

	/**
	 * Crée un nouveau serveur radio TCP écoutant sur un port spécifique et
	 * toutes les interfaces.
	 * 
	 * @param port
	 *            Le port d'écoute.
	 */
	public RadioServerTCPEngine(int port) {
		this(port, null);
	}

	/**
	 * Crée un nouveau serveur radio TCP écoutant sur un port et une interface
	 * spécifiques.
	 * 
	 * @param port
	 *            Le port d'écoute.
	 * @param iface
	 *            L'interface d'écoute.
	 */
	public RadioServerTCPEngine(int port, InetAddress iface) {
		this.port = port;
		this.iface = iface;
	}

	/**
	 * Initialisation différée du moteur de serveur radio.
	 */
	public void init(final RadioServerEngineDelegate delegate) throws IOException {
		this.delegate = delegate;

		final ServerSocket server = new ServerSocket(port, 50, iface);

		(new Thread() {
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

				// TODO: handle possible thread crash
			}
		}).start();
	}
}
