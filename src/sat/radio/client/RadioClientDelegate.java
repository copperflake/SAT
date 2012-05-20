package sat.radio.client;

import sat.radio.RadioDelegate;
import sat.utils.crypto.RSAKeyPair;

/**
 * Un délégué d'un client radio. Ce délégué est charger de gérer les différents
 * événements produits par le client radio au cours de son fonctionnement.
 */
public interface RadioClientDelegate extends RadioDelegate {
	public RSAKeyPair getLegacyTowerKey();
}
