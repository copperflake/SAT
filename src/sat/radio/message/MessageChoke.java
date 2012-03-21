package sat.radio.message;

import sat.radio.RadioID;

public class MessageChoke extends Message {
	protected MessageType type = MessageType.CHOKE;
	protected int priority = 1;

	public MessageChoke(RadioID id, int px, int py) {
		super(id, px, py);
	}

	public String toString() {
		return "I'm a Choke";
	}

	private static final long serialVersionUID = -7313650291173535659L;
}
