package sat.radio.message;

public class MessageLanding extends Message {
	protected MessageType type = MessageType.LANDINGREQUEST;
	protected int priority = 0;
	
	public MessageLanding() {
		super();
	}
	
	public String toString() {
		return "I'm a Landing!";
	}
}
