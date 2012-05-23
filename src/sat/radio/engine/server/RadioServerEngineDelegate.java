package sat.radio.engine.server;

import sat.radio.socket.RadioSocket;

/**
 * L'interface d'un délégué de serveur radio.
 */
public interface RadioServerEngineDelegate {
	/**
	 * Méthode appelée à la connexion d'un nouveau client avec un socket ouvert
	 * vers ce client.
	 */
	public void onNewConnection(RadioSocket socket);
}
