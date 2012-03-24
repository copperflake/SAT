package sat.radio.message;

import java.io.Serializable;
import java.util.Date;

import sat.radio.RadioID;

public abstract class Message implements Comparable<Message>, Serializable {
	// Global attributes
	private RadioID id;
	private int posX;
	private int posY;

	// Defaults

	private int length = 0;

	// Ces attributs ne seront pas sérialisé pour des raisons de sécurité
	// avec le flux Extended.
	transient protected int priority;
	transient protected MessageType type;

	// Additions
	protected Date time;

	public Message(RadioID i, int px, int py) {
		id = i;
		posX = px;
		posY = py;

		time = new Date();

		resetTypeAndPriority();
	}

	public void resetTypeAndPriority() {
		priority = 5;
		type = MessageType.INVALID;
	}

	public RadioID getId() {
		return id;
	}

	public int getPosX() {
		return posX;
	}

	public int getPosY() {
		return posY;
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
		if(priority < msg.getPriority()) {
			return 1;
		} else if(priority > msg.getPriority()) {
			return -1;
		} else {
			if(getTime().compareTo(msg.getTime()) > 0)
				return 1;
			else if(getTime().compareTo(msg.getTime()) < 0)
				return -1;
			else
				return 0;
		}
	}

	private static final long serialVersionUID = 145275102115030728L;
}
