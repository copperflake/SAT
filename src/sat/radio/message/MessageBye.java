package sat.radio.message;

import sat.radio.RadioID;
import sat.utils.geo.Coordinates;

public class MessageBye extends Message {
	public MessageBye(RadioID id, Coordinates c) {
		super(id, c);
	}

	public void setTypeAndPriority() {
		type = MessageType.BYE;
		priority = 4;
	}

	public String toString() {
		return "I'm a Bye!";
	}

	private static final long serialVersionUID = 421645651039774637L;
}
