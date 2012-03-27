package sat.radio.message;

import sat.radio.RadioID;
import sat.utils.geo.Coordinates;

public class MessageLanding extends Message {
	public MessageLanding(RadioID id, Coordinates c) {
		super(id, c);
	}

	public void resetTypeAndPriority() {
		type = MessageType.LANDINGREQUEST;
		priority = 2;
	}

	public String toString() {
		return "I'm a Landing!";
	}

	private static final long serialVersionUID = -7322784783202314394L;
}
