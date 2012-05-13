package sat.radio;

import sat.events.AsyncEventEmitter;
import sat.events.schedulers.PriorityEventScheduler;
import sat.utils.crypto.RSAException;
import sat.utils.crypto.RSAKeyPair;

/**
 * Une radio non-spécialisée (ni client, ni serveur). Cette classe fourni les
 * outils communs utilisés à la fois par les serveurs-radio et les
 * client-radios.
 */
public abstract class Radio extends AsyncEventEmitter {
	/**
	 * L'identifiant de cette radio. Tous les éléments d'un réseau radio SAT
	 * possède un identifiant unique l'identifiant sur le réseau.
	 */
	protected RadioID id;

	/**
	 * Les clés RSA utilisées pour crypter les communications de cette radio.
	 */
	protected RSAKeyPair keyPair;

	/**
	 * Défini le niveau de verbosité de la radio
	 */
	protected boolean verbose = false;

	/**
	 * Défini si la radio doit utiliser le cryptage
	 */
	protected boolean ciphered = true;

	/**
	 * Défini la radio ne doit pas utiliser le protocole étendu
	 */
	protected boolean legacy = false;

	/**
	 * Crée une nouvelle radio avec un label d'identifiant et une longueur de
	 * clé définie.
	 * 
	 * @param label
	 *            Le label de l'identifiant de cette radio.
	 * @param keyLength
	 *            La longueur de clé à utiliser pour le chiffrement.
	 */
	public Radio(String label, int keyLength) {
		super(new PriorityEventScheduler());

		id = new RadioID(label);

		// Key generation
		try {
			keyPair = new RSAKeyPair(keyLength);
		}
		catch(RSAException e) {
			// Invalid key length, ignore given length and use default
			keyPair = new RSAKeyPair();
		}
	}

	/**
	 * Retourne la clé utilisée par cette radio.
	 */
	public RSAKeyPair getKeyPair() {
		return keyPair;
	}
}
