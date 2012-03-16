package sat.radio.message;

public class MessageChoke extends Message {
	protected MessageType type = MessageType.CHOKE;
	protected int priority = 1;
	
	public MessageChoke() {
		super();
	}
	
	public String toString() {
		return "I'm a Choke";
	}

	private static final long serialVersionUID = -7313650291173535659L;
}
