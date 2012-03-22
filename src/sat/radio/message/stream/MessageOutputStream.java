package sat.radio.message.stream;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import sat.radio.message.Message;

/**
 * Flux de sortie de message sérialisés selon un algorithme dépendant de la
 * classe-fille utilisée.
 */
public abstract class MessageOutputStream extends FilterOutputStream {
	/**
	 * Crée un nouveau flux de sortie de messages écrivant les données
	 * serialisées dans le flux donné.
	 * 
	 * @param out
	 *            Le flux de sortie dans lequel les données serialisées seront
	 *            écrites.
	 */
	public MessageOutputStream(OutputStream out) {
		super(out);
	}

	/**
	 * Écrit un message dans le flux de message.
	 * 
	 * @param m
	 *            Le message à serialiser et à écrire dans le flux interne.
	 * 
	 * @throws IOException
	 *             L'écriture du message dans le flux interne peut générer une
	 *             exception qui est alors passée au code appelant.
	 */
	abstract public void writeMessage(Message m) throws IOException;
}
