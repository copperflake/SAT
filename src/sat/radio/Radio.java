package sat.radio;

import sat.events.AsyncEventEmitter;
import sat.events.schedulers.PriorityEventScheduler;
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
	 * Le délégué de cette radio.
	 */
	protected RadioDelegate delegate;

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
	public Radio(RadioDelegate delegate) {
		super(new PriorityEventScheduler());

		this.delegate = delegate;
		id = delegate.getRadioId();
	}

	/**
	 * Retourne la clé utilisée par cette radio tel que fournie par le délégué.
	 */
	public RSAKeyPair getKeyPair() {
		return delegate.getKeyPair();
	}
}
