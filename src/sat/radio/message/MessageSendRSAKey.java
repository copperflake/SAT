package sat.radio.message;

import sat.radio.RadioID;
import sat.utils.crypto.RSAKey;
import sat.utils.geo.Coordinates;

@SuppressWarnings("serial")
public class MessageSendRSAKey extends Message {
	private RSAKey key;

	public MessageSendRSAKey(RadioID id, Coordinates c, RSAKey key) {
		super(id, c);

		type = MessageType.SENDRSA;
		priority = 2;

		this.key = key;
	}

	public RSAKey getKey() {
		return key;
	}

	public String toString() {
		return "I'm a SendRSAKey";
	}
}
