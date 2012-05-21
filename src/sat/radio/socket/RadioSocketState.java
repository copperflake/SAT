package sat.radio.socket;

/**
 * État de la connexion sur un socket particulier.
 */
public enum RadioSocketState {
	/**
	 * Étape initiale de la connexion, le client et le serveur se mettent
	 * d'accord sur le protocole et le chiffrement à utiliser.
	 */
	HANDSHAKE,

	/**
	 * Handshake étendu.
	 */
	EXTENDED_HANDSHAKE,

	/**
	 * Étape supplémentaire nécessaire à la mise en place d'une connexion
	 * chiffrée.
	 */
	CIPHER_NEGOCIATION,

	/**
	 * Le socket est prêt pour une utilisation générale.
	 */
	READY,

	/**
	 * Le socket est en cours de fermeture.
	 */
	CLOSING
}
