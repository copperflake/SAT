package sat.radio.message;

import sat.crypto.RSAKey;

public class MessageSendRSAKey extends Message {
	protected MessageType type = MessageType.SENDRSA;
	protected int priority = 0;
	
	private RSAKey publicKey;
	
	public String toString() {
		return "I'm a SendRSAKey";
	}
	
	private static final long serialVersionUID = 7757801401504526502L;
}
