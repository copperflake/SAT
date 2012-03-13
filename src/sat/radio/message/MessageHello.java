package sat.radio.message;

public class MessageHello extends Message {
	private boolean ciphered;
	public MessageHello() {
		super();
	}
	
	public String toString() {
		return "I'm a Teapot!";
	}
}
