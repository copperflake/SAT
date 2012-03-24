package sat.radio.message.stream;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import sat.radio.message.Message;

/**
 * Flux de sortie de Message encodés avec la sérialisation Java. Cet encodage
 * est attendue par la version "étendue" du protocole de communication des
 * avions.
 */
public class ExtendedMessageOutputStream extends MessageOutputStream {
	/**
	 * Le flux de sortie d'objet Java utilisé pour l'envoi de message.
	 */
	private ObjectOutputStream oos;

	/**
	 * Construit un nouveau flux de sortie de message écrivant dans le flux de
	 * sortie spécifié.
	 * 
	 * @param out
	 *            Le flux de sortie dans lequel les messages sérialisé seront
	 *            écrits.
	 * 
	 * @throws IOException
	 *             L'initialisation du flux de sortie des objets
	 *             <code>ObjectOutputStream</code> sous-jacent peut lever une
	 *             exception qui est passée au code appelant.
	 */
	public ExtendedMessageOutputStream(OutputStream out) throws IOException {
		super(out);
		oos = new ObjectOutputStream(out);
	}

	/**
	 * Écrit un message dans le flux.
	 */
	public void writeMessage(Message m) throws IOException {
		oos.writeObject(m);
	}
}
