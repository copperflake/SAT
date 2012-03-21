package sat.radio.message;

import sat.radio.RadioID;

public class MessageKeepalive extends Message {
	protected MessageType type = MessageType.KEEPALIVE;
	protected int priority = 3;

	public MessageKeepalive(RadioID id, int px, int py) {
		super(id, px, py);
	}

	public String toString() {
		return "I'm a Keepalive!";
	}

	private static final long serialVersionUID = -5308604255489464485L;
}
