package sat.radio.message.stream;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import sat.radio.message.Message;

/**
 * Flux abstrait capable de dé-sérialiser un {@link Message} lu depuis un flux
 * d'entrée spécifié.
 * <p>
 * Il est important que le flux d'entrée corresponde au type flux de sortie
 * utilisé. Il est impossible par exemple d'utiliser un
 * <code>LegacyMessageInputStream</code> avec un
 * <code>ExtendedMessageInputStream</code>
 */
public abstract class MessageInputStream extends FilterInputStream {
	/**
	 * Crée un nouveau flux entrant lisant les données serialisées depuis le
	 * flux donné.
	 * 
	 * @param in
	 *            Le flux depuis lequel lire les données serialisée.
	 */
	public MessageInputStream(InputStream in) {
		super(in);
	}

	/**
	 * Lis un message depuis le flux d'entrée.
	 * 
	 * @return Un objet message désérialisé. Cette méthode retourne null si une
	 *         erreur est survenue pendant la lecture du message.
	 * 
	 * @throws IOException
	 *             La lecture du flux d'entrée peut générer une exception qui
	 *             est retournée au code appelant.
	 */
	public abstract Message readMessage() throws IOException;
}
