package sat.radio.engine.client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import sat.radio.socket.RadioSocket;
import sat.radio.socket.RadioSocketDirect;

/**
 * Moteur de client radio sur TCP.
 * 
 * TCP respectant parfaitement les contraintes imposées à un moteur de radio,
 * cette classe est particulièrement simple et aucune émulation n'est
 * nécessaire. Tout est géré nativement au niveau de TCP.
 */
public class RadioClientTCPEngine extends RadioClientEngine {
	/**
	 * L'adresse de la tour.
	 */
	private InetAddress host;

	/**
	 * Le port d'écoute de la tour.
	 */
	private int port;

	/**
	 * Crée un nouveau moteur de radio cliente TCP.
	 * 
	 * @param host
	 *            L'adresse de la tour.
	 * @param port
	 *            Le port de la tour.
	 */
	public RadioClientTCPEngine(InetAddress host, int port) {
		this.host = host;
		this.port = port;
	}

	/**
	 * Initialise le moteur de radio et retourne un socket vers la tour.
	 */
	public RadioSocket init() throws IOException {
		Socket s = new Socket(host, port);
		return new RadioSocketDirect(s.getInputStream(), s.getOutputStream());
	}
}
