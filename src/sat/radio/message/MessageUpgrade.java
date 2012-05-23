package sat.radio.message;

import sat.radio.RadioID;
import sat.utils.geo.Coordinates;

@SuppressWarnings("serial")
public class MessageUpgrade extends Message {
	public MessageUpgrade(RadioID id, Coordinates c) {
		super(id, c);

		type = MessageType.UPGRADE;
		priority = 1;
	}

	public String toString() {
		return "I'm a Upgrade!";
	}
}
