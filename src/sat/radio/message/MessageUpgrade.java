package sat.radio.message;

import sat.radio.RadioID;
import sat.utils.geo.Coordinates;

public class MessageUpgrade extends Message {
	public MessageUpgrade(RadioID id, Coordinates c) {
		super(id, c);

		type = MessageType.UPGRADE;
		priority = 1;
	}

	public String toString() {
		return "I'm a Upgrade!";
	}

	private static final long serialVersionUID = -4151810452089768804L;
}
