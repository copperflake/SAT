package sat.radio;

import java.io.FileNotFoundException;

import sat.radio.message.Message;
import sat.utils.crypto.RSAException;
import sat.utils.crypto.RSAKeyPair;

/**
 * Une radio non-spécialisée (ni client, ni serveur). Cette classe fourni les
 * outils communs utilisés à la fois par les serveurs-radio et les
 * client-radios.
 */

public abstract class Radio {
	/**
	 * L'identifiant de cette radio. Tous les éléments d'un réseau radio SAT
	 * possède un identifiant unique l'identifiant sur le réseau.
	 */
	protected RadioID id;

	/**
	 * Les clés RSA utilisées pour crypter les communications de cette radio.
	 */
	protected RSAKeyPair keyPair;

	public Radio(String label, int keyLength) {
		id = new RadioID(label);
		try {
			keyPair = new RSAKeyPair(keyLength);
		}
		catch(RSAException e) {
			// Invalid key length, ignore
			keyPair = new RSAKeyPair();
		}
	}

	public RSAKeyPair getKeyPair() {
		return keyPair;
	}

	public void sendFile(String path, String dest) throws FileNotFoundException {
		//SegmentableFile file = new SegmentableFile(path);
		int i = 0;
		//while(file.iterator().hasNext()) {
		int px = 0;
		int py = 0;
		// TODO: do
		//MessageData message = new MessageData(id, px, py, file.getHash(), i++, file.getFormat(), file.getSize(), file.iterator().next());
		//}
	}

	protected void send(Message msg, String dest) {

	}
}
