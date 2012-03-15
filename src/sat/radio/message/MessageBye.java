package sat.radio.message;

public class MessageBye extends Message {
	protected MessageType type = MessageType.BYE;
	protected int priority = 0;
	
	public MessageBye() {
		super();
	}
	
	public String toString() {
		return "I'm a Bye!";
	}
}
