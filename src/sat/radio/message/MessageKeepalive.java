package sat.radio.message;

public class MessageKeepalive extends Message {
	protected MessageType type = MessageType.KEEPALIVE;
	protected int priority = 3;
	
	public MessageKeepalive() {
		super();
	}
	
	public String toString() {
		return "I'm a Keepalive!";
	}
	
	private static final long serialVersionUID = -5308604255489464485L;
}
