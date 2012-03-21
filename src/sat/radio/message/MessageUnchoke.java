package sat.radio.message;

import sat.radio.RadioID;

public class MessageUnchoke extends Message {
	protected MessageType type = MessageType.UNCHOKE;
	protected int priority = 4;

	public MessageUnchoke(RadioID id, int px, int py) {
		super(id, px, py);
	}

	public String toString() {
		return "I'm a Unchoke!";
	}

	private static final long serialVersionUID = -4086684315280907680L;
}
