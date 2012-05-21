package sat.radio.message;

import sat.radio.RadioID;
import sat.utils.geo.Coordinates;

public class MessageHello extends Message {
	private boolean ciphered;
	private boolean extended;

	public MessageHello(RadioID id, Coordinates c, boolean ciphered, boolean extended) {
		super(id, c);

		type = MessageType.HELLO;
		priority = 1;

		this.ciphered = ciphered;
		this.extended = extended;
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
