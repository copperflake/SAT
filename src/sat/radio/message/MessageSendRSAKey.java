package sat.radio.message;

import sat.crypto.RSAKey;
import sat.radio.RadioID;

public class MessageSendRSAKey extends Message {
	private RSAKey key;

	public MessageSendRSAKey(RadioID i, int px, int py, RSAKey key) {
		super(i, px, py);

		type = MessageType.SENDRSA;
		priority = 2;

		this.key = key;
	}

	public String toString() {
		return "I'm a SendRSAKey";
	}

	public RSAKey getKey() {
		return key;
	}

	private static final long serialVersionUID = 7757801401504526502L;
}
