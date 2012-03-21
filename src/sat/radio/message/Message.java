package sat.radio.message;

import java.io.Serializable;
import java.util.Date;

import sat.radio.RadioID;

public abstract class Message implements Comparable<Message>, Serializable {
	// Global attributes
	private RadioID id;
	private int posx;
	private int posy;

	// Defaults

	private int length = 0;
	protected int priority = 5;
	protected MessageType type = MessageType.INVALID;

	// Additions
	protected Date time;

	public Message(RadioID i, int px, int py) {
		id = i;
		posx = px;
		posy = py;

		time = new Date();
	}

	public RadioID getId() {
		return id;
	}

	public int getPosx() {
		return posx;
	}

	public int getPosy() {
		return posy;
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
