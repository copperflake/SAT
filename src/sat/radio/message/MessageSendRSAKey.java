package sat.radio.message;

import sat.radio.RadioID;
import sat.utils.crypto.RSAKey;
import sat.utils.geo.Coordinates;

public class MessageSendRSAKey extends Message {
	private RSAKey key;

	public MessageSendRSAKey(RadioID i, Coordinates c, RSAKey key) {
		super(i, c);

		this.key = key;
	}

	public void resetTypeAndPriority() {
		type = MessageType.SENDRSA;
		priority = 2;
	}

	public String toString() {
		return "I'm a SendRSAKey";
	}

	public RSAKey getKey() {
		return key;
	}

	private static final long serialVersionUID = 7757801401504526502L;
}
