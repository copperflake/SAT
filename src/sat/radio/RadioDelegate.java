package sat.radio;

import sat.utils.crypto.RSAKeyPair;
import sat.utils.geo.Coordinates;

/**
 * Délégué d'une radio générique.
 */
public interface RadioDelegate {
	/**
	 * Demande au délégué sa position.
	 */
	public Coordinates getLocation();

	/**
	 * Demande au délégué sa clé de chiffrement
	 */
	public RSAKeyPair getKeyPair();
}
