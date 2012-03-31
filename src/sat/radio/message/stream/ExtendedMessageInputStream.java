package sat.radio.message.stream;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

import sat.radio.message.Message;

/**
 * Flux d'entrée de messages codés avec la serialisation native de Java. Cet
 * encodage est attendu lors d'une communication utilisant le protocol étendu.
 */
public class ExtendedMessageInputStream extends MessageInputStream {
	/**
	 * Le flux d'entrée d'objet qui sera utilisé pour transmettre les messages.
	 */
	private ObjectInputStream ois;

	/**
	 * Crée un nouveau flux d'entrée de messages serialisés.
	 * 
	 * @param in
	 *            Le flux d'entrée qui sera lu pour obtenir les données
	 *            serialisées.
	 * 
	 * @throws IOException
	 *             L'initialisation du flux d'entrée de messages serialisés
	 *             <code>ObjectInputStream</code> peut lever une exception qui
	 *             est passée au code appelant.
	 */
	public ExtendedMessageInputStream(InputStream in) throws IOException {
		super(in);
		ois = new ObjectInputStream(in);
	}

	/**
	 * Lis un message depuis le flus de messages. Cette méthode est permissive
	 * sur la qualité du flux entrant: si des données invalides sont lues, elle
	 * seront ignorée et la méthode attendra un autre message valide avant de
	 * retourner un résultat. Les erreurs de lecture de type
	 * <code>IOException</code> seront en revanche relancée.
	 */
	public Message readMessage() throws IOException {
		try {
			// Met fin à la boucle et retourne le premier message valide
			// obtenu.
			Message message = (Message) ois.readObject();
			message.setTypeAndPriority(); // Reset type and priority
			return message;
		} catch(IOException e) {
			// Rethrow IOExceptions
			throw e;
		} catch(Exception e) {
			// Ignore bad objects...
			return null;
		}
	}
}
