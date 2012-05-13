package sat.radio.message;

import java.io.Serializable;
import java.util.Date;

import sat.events.Event;
import sat.radio.RadioID;
import sat.utils.geo.Coordinates;

public abstract class Message extends Event implements Serializable, Comparable<Message> {
	// Global attributes
	private RadioID id;
	private Coordinates coords;

	// Defaults

	private int length = 0;

	// Ces attributs ne seront pas sérialisé pour des raisons de sécurité
	// avec le flux Extended.
	transient protected int priority;
	transient protected MessageType type;

	// Additions
	protected Date time;

	public Message(RadioID i, Coordinates c) {
		id = i;
		coords = c;

		time = new Date();

		setTypeAndPriority();
	}

	public void setTypeAndPriority() {
		priority = 5;
		type = MessageType.INVALID;
	}

	public RadioID getID() {
		return id;
	}

	public Coordinates getCoordinates() {
		return coords;
	}

	public int getLength() {
		return length;
	}

	public int getPriority() {
		return priority;
	}

	public MessageType getType() {
		return type;
	}

	// Compare tools

	public Date getTime() {
		return time;
	}

	public int compareTo(Message msg) {
		if(priority > msg.getPriority()) {
			return 1;
		}
		else if(priority < msg.getPriority()) {
			return -1;
		}
		else {
			return getTime().compareTo(msg.getTime());
		}
	}

	private static final long serialVersionUID = 145275102115030728L;
}
