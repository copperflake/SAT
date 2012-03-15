package sat.radio.message;

public class MessageKeepalive extends Message {
	protected MessageType type = MessageType.KEEPALIVE;
	protected int priority = 0;
	
	public MessageKeepalive() {
		super();
	}
	
	public String toString() {
		return "I'm a Keepalive!";
	}
}
