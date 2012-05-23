package sat.radio.message;

import sat.radio.RadioID;
import sat.utils.geo.Coordinates;

@SuppressWarnings("serial")
public class MessageKeepalive extends Message {
	public MessageKeepalive(RadioID id, Coordinates c) {
		super(id, c);

		type = MessageType.KEEPALIVE;
		priority = 3;
	}

	public String toString() {
		return "I'm a Keepalive!";
	}
}
