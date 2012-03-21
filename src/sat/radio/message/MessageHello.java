package sat.radio.message;

import sat.radio.RadioID;

public class MessageHello extends Message {
	protected MessageType type = MessageType.HELLO;
	protected int priority = 1;

	private boolean ciphered;

	public MessageHello(RadioID id, int px, int py) {
		super(id, px, py);
	}

	public String toString() {
		return "I'm a Teapot!";
	}

	private static final long serialVersionUID = 6638823010239251082L;
}
