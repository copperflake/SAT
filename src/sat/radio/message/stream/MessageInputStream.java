package sat.radio.message.stream;

import java.io.DataInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;

import sat.radio.RadioID;
import sat.radio.message.*;
import sat.utils.crypto.RSAKey;
import sat.utils.geo.Coordinates;
import sat.utils.routes.MoveType;
import sat.utils.routes.RoutingType;
import sat.utils.routes.Waypoint;

/**
 * Flux d'entrée de message radio.
 */
public class MessageInputStream extends FilterInputStream {
	/**
	 * Un flux d'entrée de données utilisé pour lire les différents composants
	 * du message sérialisé.
	 */
	private DataInputStream dis;

	/**
	 * Indique si le flux utilise le mode étendu.
	 */
	private boolean extended = false;

	/**
	 * La longueur maximal qu'un bloc de taille variable peut nécessiter avant
	 * que la tour estime qu'il y a une erreur de communication.
	 */
	private static final int MAX_VARLENGTH_INPUT = 2048;

	/**
	 * Crée un nouveau flux d'entrée de message ITP-compliant.
	 * 
	 * @param in
	 *            Le flux d'entrée depuis lequel lire les données serialisées.
	 */
	public MessageInputStream(InputStream in) {
		super(in);

		// Le message est sérialisé en tant que séquence de primitives Java 
		// qui pourront être lues avec un DataInputStream.
		dis = new DataInputStream(in);
	}

	/**
	 * Lis un message depuis le flux d'entrée
	 */
	@SuppressWarnings("unused")
	public synchronized Message readMessage() throws IOException {
		// Lecture des paramètres communs à tous les messages.
		// Attention, l'ordre de lecture est important ! (obviously)

		// PlaneID
		RadioID id;
		if(extended) {
			int idLength = dis.readInt();
			id = (RadioID) Serializer.deserialize(fill(new byte[idLength]));
		}
		else {
			id = new RadioID(fill(new byte[8]));
		}

		int length = dis.readInt();
		int priority = dis.readInt(); // Not used...

		float px, py, pz;
		if(extended) {
			px = dis.readFloat();
			py = dis.readFloat();
			pz = dis.readFloat();
		}
		else {
			px = dis.readInt();
			py = dis.readInt();
			pz = -1f;
		}

		Coordinates c = new Coordinates(px, py, -1);

		// Le type du message
		MessageType type;
		try {
			type = MessageType.values()[dis.readInt()];
		}
		catch(RuntimeException e) {
			// Error with type deserialization
			throw new IOException("Invalid message type");
		}

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
				byte[] hash = fill(20);
				int continuation = dis.readInt();

				String format;
				if(this.extended) {
					// TODO: why serializing?
					int formatLength = dis.readInt();
					format = (String) Serializer.deserialize(fill(formatLength));
				}
				else {
					format = new String(fill(4));
				}

				int fileSize = dis.readInt();
				byte[] payload = fill(length);

				message = new MessageData(id, c, hash, continuation, format, fileSize, payload);
				break;

			case MAYDAY:
				String cause = new String(fill(length));
				message = new MessageMayDay(id, c, cause);
				break;

			case SENDRSA:
				// What's that ?
				int keySize = dis.readInt();

				int modulusLength = dis.readInt();
				byte[] modulus = fill(modulusLength);

				int publicKeyLength = dis.readInt();
				byte[] publicKey = fill(publicKeyLength);

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
				RoutingType routingType = RoutingType.values()[dis.readInt()];
				MoveType moveType = MoveType.values()[dis.readInt()];

				float circularAngle = 0;

				if(length > 0) {
					if(this.extended) {
						circularAngle = dis.readFloat();
					}
					else {
						circularAngle = dis.readInt();
					}
				}

				float args[];

				if(length > 0) {
					args = new float[]{c.getX(), c.getY(), c.getZ(), circularAngle};
				} else {
					args = c.toFloats();
				}

				Waypoint waypoint = new Waypoint(moveType, args);

				message = new MessageRouting(id, waypoint, routingType);
				break;

			case KEEPALIVE:
				message = new MessageKeepalive(id, c);
				break;

			case LANDINGREQUEST:
				message = new MessageLanding(id, c);
				break;

			case UPGRADE:
				message = new MessageUpgrade(id, c);
				break;
		}

		if(message == null)
			throw new IOException("Unable to deserialize message");

		return message;
	}

	/**
	 * Replis un buffer donné.
	 * 
	 * Cette méthode est normalement utilisée au travers du wrapper fill(int)
	 * qui demande simplement la longueur du bloc de données à lire sans
	 * l'allouer, et vérifie que cette taille ne soit pas trop importante.
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

	/**
	 * Remplis un nouveau buffer de taille donnée. Voir fill(byte[]).
	 */
	private byte[] fill(int length) throws IOException {
		if(length > MAX_VARLENGTH_INPUT) {
			throw new IOException("Varlength data block is too big");
		}

		return fill(new byte[length]);
	}

	/**
	 * Indique si le flux est étendu.
	 */
	public boolean isExtended() {
		return extended;
	}

	/**
	 * Défini si le flux doit utiliser le mode étendu.
	 */
	public synchronized void setExtended(boolean extended) {
		this.extended = extended;
	}
}
