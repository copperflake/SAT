package sat.radio.message;

import sat.radio.RadioID;
import sat.utils.geo.Coordinates;

public class MessageChoke extends Message {
	public MessageChoke(RadioID id, Coordinates c) {
		super(id, c);

		type = MessageType.CHOKE;
		priority = 1;
	}

	public String toString() {
		return "I'm a Choke";
	}

	private static final long serialVersionUID = -7313650291173535659L;
}
