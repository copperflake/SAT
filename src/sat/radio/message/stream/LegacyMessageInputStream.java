package sat.radio.message.stream;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;

import sat.radio.RadioID;
import sat.radio.message.*;
import sat.utils.crypto.RSAKey;
import sat.utils.geo.Coordinates;

/**
 * Flux d'entrée de message compatible avec le protocole de sérialisation de
 * l'ITP.
 */
public class LegacyMessageInputStream extends MessageInputStream {
	/**
	 * Un flux d'entrée de données utilisé pour lire les différents composants
	 * du message sérialisé.
	 */
	private DataInputStream dis;

	/**
	 * Crée un nouveau flux d'entrée de message ITP-compliant.
	 * 
	 * @param in
	 *            Le flux d'entrée depuis lequel lire les données serialisées.
	 */
	public LegacyMessageInputStream(InputStream in) {
		super(in);

		// Le message est sérialisé en tant que séquence de primitives Java 
		// qui pourront être lues avec un DataInputStream.
		dis = new DataInputStream(in);
	}

	/**
	 * Lis un message depuis le flux d'entrée
	 */
	@SuppressWarnings("unused")
	public Message readMessage() throws IOException {
		// Lecture des paramètres communs à tous les messages.
		// Attention, l'ordre de lecture est important ! (obviously)
		RadioID id = new RadioID(fill(new byte[8])); // PlaneID

		int length = dis.readInt();
		int priority = dis.readInt(); // Not used...

		int px = dis.readInt();	// PosX
		int py = dis.readInt();	// PoxY

		Coordinates c = new Coordinates(px, py, 0);

		// Le type du message
		MessageType type = MessageType.values()[dis.readInt()];

		// Message qui sera retourné.
		Message message = null;

		switch(type) {
			case HELLO:
				byte reserved = dis.readByte();

				boolean ciphered = (reserved & (1 << 4)) != 0;
				boolean extended = (reserved & (1 << 7)) != 0;

				message = new MessageHello(id, c, ciphered, extended);
				break;

			case DATA:
				byte[] hash = fill(new byte[20]);
				int continuation = dis.readInt();
				byte[] format = fill(new byte[4]);
				int fileSize = dis.readInt();
				byte[] payload = fill(new byte[length]);

				message = new MessageData(id, c, hash, continuation, format, fileSize, payload);
				break;

			case MAYDAY:
				String cause = new String(fill(new byte[length]));
				message = new MessageMayDay(id, c, cause);
				break;

			case SENDRSA:
				// What's that ?
				int keySize = dis.readInt();

				int modulusLength = dis.readInt();
				byte[] modulus = fill(new byte[modulusLength]);

				int publicKeyLength = dis.readInt();
				byte[] publicKey = fill(new byte[publicKeyLength]);

				RSAKey key = new RSAKey(new BigInteger(publicKey), new BigInteger(modulus));

				message = new MessageSendRSAKey(id, c, key);
				break;

			case CHOKE:
				message = new MessageChoke(id, c);
				break;

			case UNCHOKE:
				message = new MessageUnchoke(id, c);
				break;

			case BYE:
				message = new MessageBye(id, c);
				break;

			case ROUTING:
				int routingMessageType = dis.readInt();
				int moveType = dis.readInt();
				byte[] payload2 = fill(new byte[length]);
				// TODO data

				message = new MessageRouting(id, c, null);
				break;

			case KEEPALIVE:
				message = new MessageKeepalive(id, c);
				break;

			case LANDINGREQUEST:
				message = new MessageLanding(id, c);
				break;
		}

		if(message == null)
			throw new IOException("Unable to deserialize message");

		return message;
	}

	/**
	 * Replis un buffer donné.
	 * 
	 * @param buffer
	 *            Un buffer de byte qui sera rempli avec les bytes disponible
	 *            dans le flux d'entrée interne.
	 * 
	 * @return Le buffer passé en paramètre. Cette valeur de retour est le même
	 *         buffer que celui passé en paramètre. Elle est disponible afin de
	 *         simplifier la syntaxe d'utilisation de cette méthode.
	 * 
	 * @throws IOException
	 *             Cette méthode lit des bytes depuis le flux interne. Cette
	 *             lecture est succeptible de lever une exception.
	 */
	private byte[] fill(byte[] buffer) throws IOException {
		dis.readFully(buffer);
		return buffer;
	}
}
