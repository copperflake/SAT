package sat.radio.message;

public class MessageChoke extends Message {
	protected MessageType type = MessageType.CHOKE;
	protected int priority = 0;
	
	public MessageChoke() {
		super();
	}
	
	public String toString() {
		return "I'm a Choke";
	}
}
