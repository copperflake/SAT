package sat.radio.message;

import sat.radio.RadioID;

public class MessageHello extends Message {
	private boolean ciphered;

	public MessageHello(RadioID id, int px, int py, byte reserved) {
		super(id, px, py);

		ciphered = (reserved & (1 << 4)) != 0;
	}

	public void resetTypeAndPriority() {
		type = MessageType.HELLO;
		priority = 1;
	}

	/**
	 * @return le byte <code>reserved</code> du protocole ITP.
	 */
	public byte getReserved() {
		return (byte) (ciphered ? 1 << 4 : 0);
	}

	public String toString() {
		return "I'm a Teapot!";
	}

	private static final long serialVersionUID = 6638823010239251082L;
}
