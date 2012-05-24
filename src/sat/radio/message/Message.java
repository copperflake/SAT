package sat.radio.message;

import java.util.Date;

import sat.events.PriorityEvent;
import sat.radio.RadioID;
import sat.utils.geo.Coordinates;

/**
 * Un message radio qui sert d'unité de communication entre la tour et les
 * avions. Cette classe abstraite est ensuite étendue pour définir les types de
 * messages réels.
 */
@SuppressWarnings("serial")
public abstract class Message extends PriorityEvent<Message> {
	/**
	 * L'ID de l'émetteur de ce message.
	 */
	private RadioID id;

	/**
	 * Les coordonnées actuelles de l'émetteur, sauf dans le cas d'un
	 * MessageRouting où ce champ défini les coordonnées de routage.
	 * 
	 * [ITP is funny]
	 */
	private Coordinates coords;

	/**
	 * Horodatage de la réception du message.
	 */
	private Date date;

	/**
	 * La longueur du payload du packet. En général non-utilisée sauf pour les
	 * message MayDay, Routing et Data.
	 */
	protected int length = 0;

	/**
	 * La priorité du message, plus elle est basse plus le message est
	 * important.
	 */
	protected int priority = 5;

	/**
	 * Le type de message.
	 */
	protected MessageType type = MessageType.INVALID;

	/**
	 * Construit un nouveau message.
	 * 
	 * @param i
	 *            L'identifiant RadioID de l'émetteur.
	 * @param c
	 *            Les coordonnées de l'émetteur ou les coordonnées de routage
	 *            dans le cas d'un message Routing. [lawl]
	 */
	public Message(RadioID i, Coordinates c) {
		id = i;
		coords = c;

		messageID = getNextMessageID();
		date = new Date();
	}

	/**
	 * Retourne l'ID de l'émetteur du message.
	 */
	public RadioID getID() {
		return id;
	}

	/**
	 * Retourne les coordonnées de l'émetteur du message.
	 */
	public Coordinates getCoordinates() {
		return coords;
	}

	/**
	 * Retourne la longueur du payload du message.
	 */
	public int getLength() {
		return length;
	}

	/**
	 * Retourne la priorité du message.
	 */
	public int getPriority() {
		return priority;
	}

	/**
	 * Retourne le type du message.
	 */
	public MessageType getType() {
		return type;
	}

	/**
	 * Retourne l'horodatage de réception du message.
	 */
	public Date getDate() {
		return date;
	}

	/**
	 * Compare deux messages et détermine le plus prioritaire. Une valeur de
	 * retour de -1 indique un message plus prioritaire que celui auquel il est
	 * comparé.
	 */
	public int compareTo(Message msg) {
		if(priority > msg.getPriority()) {
			return 1;
		}
		else if(priority < msg.getPriority()) {
			return -1;
		}
		else {
			return (messageID > msg.messageID) ? 1 : -1;
		}
	}

	// - - - Tickets system - - -

	/**
	 * L'ID de séquence du prochain message. Utilisé pour ordonner les messages
	 * de façon FIFO dans le cas d'une égalité de priorité.
	 */
	private static long nextMessageID = 0;

	/**
	 * L'ID de séquence de ce message.
	 */
	private long messageID;

	/**
	 * Génère le prochain numéro de séquence.
	 */
	private synchronized static long getNextMessageID() {
		return nextMessageID++;
	}
}
