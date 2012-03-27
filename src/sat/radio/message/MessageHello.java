package sat.radio.message;

import sat.radio.RadioID;

public class MessageHello extends Message {
	private boolean ciphered;
	private boolean extended;

	public MessageHello(RadioID id, int px, int py, boolean ciphered, boolean extended) {
		super(id, px, py);

		this.ciphered = ciphered;
		this.extended = extended;
	}

	public void resetTypeAndPriority() {
		type = MessageType.HELLO;
		priority = 1;
	}

	public boolean isCiphered() {
		return ciphered;
	}

	public boolean isExtended() {
		return extended;
	}

	public String toString() {
		return "I'm a Teapot!";
	}

	private static final long serialVersionUID = 6638823010239251082L;
}
