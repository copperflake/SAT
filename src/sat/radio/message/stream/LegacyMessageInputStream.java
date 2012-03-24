package sat.radio.message.stream;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;

import sat.crypto.RSAKey;
import sat.radio.RadioID;
import sat.radio.message.*;

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

		// Le type du message
		MessageType type = MessageType.values()[dis.readInt()];

		// Message qui sera retourné.
		Message message = null;

		switch(type) {
			case HELLO:
				byte reserved = dis.readByte();
				message = new MessageHello(id, px, py, reserved);
				break;

			case DATA:
				byte[] hash = fill(new byte[20]);
				int continuation = dis.readInt();
				byte[] format = fill(new byte[4]);
				int fileSize = dis.readInt();
				byte[] payload = fill(new byte[length]);

				message = new MessageData(id, px, py, hash, continuation, format, fileSize, payload);
				break;

			case MAYDAY:
				String cause = new String(fill(new byte[length]));
				message = new MessageMayDay(id, px, py, cause);
				break;

			case SENDRSA:
				// What's that ?
				int keySize = dis.readInt();

				int modulusLength = dis.readInt();
				byte[] modulus = fill(new byte[modulusLength]);

				int publicKeyLength = dis.readInt();
				byte[] publicKey = fill(new byte[publicKeyLength]);

				RSAKey key = new RSAKey(new BigInteger(publicKey), new BigInteger(modulus));

				message = new MessageSendRSAKey(id, px, py, key);
				break;

			case CHOKE:
				message = new MessageChoke(id, px, py);
				break;

			case UNCHOKE:
				message = new MessageUnchoke(id, px, py);
				break;

			case BYE:
				message = new MessageBye(id, px, py);
				break;

			case ROUTING:
				int routingMessageType = dis.readInt();
				int moveType = dis.readInt();
				byte[] payload2 = fill(new byte[length]);
				// TODO data

				message = new MessageRouting(id, px, py, null);
				break;

			case KEEPALIVE:
				message = new MessageKeepalive(id, px, py);
				break;

			case LANDINGREQUEST:
				message = new MessageLanding(id, px, py);
				break;
		}

		return message;
	}

	/**
	 * Replis un buffer donné. En d'autre termes: lis autant de bytes que la
	 * taille du buffer passé en paramètre depuis le flux d'entrée interne et
	 * les écrits dans ce même buffer.
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
		for(int i = 0; i < buffer.length; i++) {
			buffer[i] = dis.readByte();
		}

		return buffer;
	}
}
