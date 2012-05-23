package sat.radio.message;

import sat.radio.RadioID;
import sat.utils.geo.Coordinates;

@SuppressWarnings("serial")
public class MessageUnchoke extends Message {
	public MessageUnchoke(RadioID id, Coordinates c) {
		super(id, c);

		type = MessageType.UNCHOKE;
		priority = 4;
	}

	public String toString() {
		return "I'm an Unchoke!";
	}
}
