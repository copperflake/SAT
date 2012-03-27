package sat.radio.message;

import sat.radio.RadioID;
import sat.utils.geo.Coordinates;

public class MessageKeepalive extends Message {
	public MessageKeepalive(RadioID id, Coordinates c) {
		super(id, c);
	}

	public void resetTypeAndPriority() {
		type = MessageType.KEEPALIVE;
		priority = 3;
	}

	public String toString() {
		return "I'm a Keepalive!";
	}

	private static final long serialVersionUID = -5308604255489464485L;
}
