package sat.radio.message;

import java.io.Serializable;
import java.util.Date;

import sat.radio.RadioID;

public abstract class Message implements Comparable<Message>, Serializable {
	protected RadioID id;

	protected Date time;
	
	protected MessageType type = MessageType.INVALID;
	protected int priority = 5;
	
	public Message() {
		this.time = new Date();
	}
	
	public MessageType getType() {
		return type;
	}
	
	public int getPriority() {
		return priority;
	}
	
	public Date getDate() {
		return time;
	}

	public abstract String toString();
	
	public int compareTo(Message msg) {
		if(this.priority < msg.getPriority()) {
			return 1;
		} else if(this.priority > msg.getPriority()) {
			return -1;
		} else {
			if(this.getDate().compareTo(msg.getDate()) > 0)
				return 1;
			else if(this.getDate().compareTo(msg.getDate()) < 0)
				return -1;
			else
				return 0;
		}
	}
	
	private static final long serialVersionUID = 145275102115030728L;
}
