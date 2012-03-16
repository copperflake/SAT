package sat.radio.message;

public class MessageBye extends Message {
	protected MessageType type = MessageType.BYE;
	protected int priority = 4;
	
	public MessageBye() {
		super();
	}
	
	public String toString() {
		return "I'm a Bye!";
	}
	
	private static final long serialVersionUID = 421645651039774637L;
}
