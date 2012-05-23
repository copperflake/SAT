package sat.radio.engine.client;

import java.io.IOException;

import sat.radio.engine.RadioEngine;
import sat.radio.socket.RadioSocket;

/**
 * Un moteur de radio pour un client radio.
 */
public abstract class RadioClientEngine extends RadioEngine {
	/**
	 * Initialisation différée du moteur de radio. La radio effectue elle-même
	 * l'initialisation du moteur qui lui est passé en paramètre. Il n'est donc
	 * pas possible d'utiliser simplement un constructeur.
	 * 
	 * Retourne un socket vers la tour de contrôle.
	 */
	public abstract RadioSocket init() throws IOException;
}
