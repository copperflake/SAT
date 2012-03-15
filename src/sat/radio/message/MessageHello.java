package sat.radio.message;

public class MessageHello extends Message {
	protected MessageType type = MessageType.HELLO;
	protected int priority = 0;
	
	private boolean ciphered;
	public MessageHello() {
		super();
	}
	
	public String toString() {
		return "I'm a Teapot!";
	}
}
