package sat.radio.message;

import sat.crypto.RSAKey;
import sat.radio.RadioID;

public class MessageSendRSAKey extends Message {
	private RSAKey publicKey;

	public MessageSendRSAKey(RadioID i, int px, int py) {
		super(i, px, py);

		type = MessageType.SENDRSA;
		priority = 2;
	}

	public String toString() {
		return "I'm a SendRSAKey";
	}

	private static final long serialVersionUID = 7757801401504526502L;
}
