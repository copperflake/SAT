package sat.radio.message;

import sat.crypto.RSAKey;
import sat.radio.RadioID;

public class MessageSendRSAKey extends Message {
	public MessageSendRSAKey(RadioID i, int px, int py) {
		super(i, px, py);
	}

	protected MessageType type = MessageType.SENDRSA;
	protected int priority = 2;

	private RSAKey publicKey;

	public String toString() {
		return "I'm a SendRSAKey";
	}

	private static final long serialVersionUID = 7757801401504526502L;
}
