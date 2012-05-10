package sat.radio.message;

import java.io.Serializable;
import java.util.Date;

import sat.radio.RadioID;
import sat.radio.RadioProtocolException;
import sat.radio.message.handler.MessageHandler;
import sat.utils.geo.Coordinates;

public abstract class Message implements Comparable<Message>, Serializable {
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

	// Visitor pattern

	public abstract void handle(MessageHandler handler) throws RadioProtocolException;

	// Compare tools

	public Date getTime() {
		return time;
	}

	public int compareTo(Message msg) {
		if(priority < msg.getPriority()) {
			return 1;
		}
		else if(priority > msg.getPriority()) {
			return -1;
		}
		else {
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
