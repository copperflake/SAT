package sat.com;

import java.util.Date;

public abstract class Message implements Comparable<Message> {
	private RadioID id;
	private int priority;
	private int posx, posy;
	private int length;
	private Date time;
	private MessageType type;
	
	public Message(MessageType mtype) {
		this.type = mtype;
		// this.type = MessageType.values()[mtype];
	}
	
	public int getPriority() {
		return this.priority;
	}
	
	public Date getDate() {
		return this.time;
	}
	
	public int compareTo(Message msg) {
		if(this.priority < msg.getPriority())
			return 1;
		else if(this.priority > msg.getPriority())
			return -1;
		else {
			if(this.getDate().compareTo(msg.getDate()) > 0)
				return 1;
			else if(this.getDate().compareTo(msg.getDate()) < 0)
				return -1;
			else
				return 0;
		}
	}
}
