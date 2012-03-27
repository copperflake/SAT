package sat.radio.message;

import sat.radio.RadioID;
import sat.utils.geo.Coordinates;

public class MessageUnchoke extends Message {
	public MessageUnchoke(RadioID id, Coordinates c) {
		super(id, c);
	}

	public void resetTypeAndPriority() {
		type = MessageType.UNCHOKE;
		priority = 4;
	}

	public String toString() {
		return "I'm a Unchoke!";
	}

	private static final long serialVersionUID = -4086684315280907680L;
}
