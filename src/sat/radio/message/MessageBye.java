package sat.radio.message;

import sat.radio.RadioID;
import sat.utils.geo.Coordinates;

@SuppressWarnings("serial")
public class MessageBye extends Message {
	public MessageBye(RadioID id, Coordinates c) {
		super(id, c);

		type = MessageType.BYE;
		priority = 3;
	}

	public String toString() {
		return "I'm a Bye!";
	}
}
