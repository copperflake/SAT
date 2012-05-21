package sat.radio.message;

import sat.radio.RadioID;
import sat.utils.geo.Coordinates;

public class MessageRouting extends Message {

	public MessageRouting(RadioID id, Coordinates c) {
		super(id, c);

		type = MessageType.ROUTING;
		priority = 2;
	}

	public String toString() {
		return "I'm a routing!";
	}

	private static final long serialVersionUID = -8493801549147991470L;
}
