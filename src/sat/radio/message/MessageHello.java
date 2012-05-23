package sat.radio.message;

import sat.radio.RadioID;
import sat.utils.geo.Coordinates;

@SuppressWarnings("serial")
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
		return "I'm a Hello!";
	}
}
