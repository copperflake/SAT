package sat.radio;

import java.io.FileNotFoundException;

import sat.file.SegmentableFile;
import sat.radio.message.Message;

/**
 * Une radio non-spécialisée (ni client, ni serveur). Cette classe fourni
 * les outils communs utilisés à la fois par les serveurs-radio et les
 * client-radios.
 */
public abstract class Radio {
	/**
	 * L'identifiant de cette radio. Tous les éléments d'un réseau radio
	 * SAT possède un identifiant unique l'identifiant sur le réseau.
	 */
	protected RadioID id;
	
	public void file(String path, String dest) throws FileNotFoundException {
		SegmentableFile file = new SegmentableFile(path);
	}
	
	protected void send(Message msg, String dest) {
		
	}
}
