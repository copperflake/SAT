package sat.radio.message;

import sat.radio.RadioID;

public class MessageChoke extends Message {
	public MessageChoke(RadioID id, int px, int py) {
		super(id, px, py);

		type = MessageType.CHOKE;
		priority = 1;
	}

	public String toString() {
		return "I'm a Choke";
	}

	private static final long serialVersionUID = -7313650291173535659L;
}
