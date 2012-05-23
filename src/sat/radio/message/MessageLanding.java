package sat.radio.message;

import sat.radio.RadioID;
import sat.utils.geo.Coordinates;

@SuppressWarnings("serial")
public class MessageLanding extends Message {
	public MessageLanding(RadioID id, Coordinates c) {
		super(id, c);

		type = MessageType.LANDINGREQUEST;
		priority = 2;
	}

	public String toString() {
		return "I'm a Landing!";
	}
}
