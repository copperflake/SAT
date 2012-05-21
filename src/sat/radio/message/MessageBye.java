package sat.radio.message;

import sat.radio.RadioID;
import sat.utils.geo.Coordinates;

public class MessageBye extends Message {
	public MessageBye(RadioID id, Coordinates c) {
		super(id, c);

		type = MessageType.BYE;
		priority = 3;
	}

	public String toString() {
		return "I'm a Bye!";
	}

	private static final long serialVersionUID = 421645651039774637L;
}
