package sat.radio.message;

public class MessageLanding extends Message {
	protected MessageType type = MessageType.LANDINGREQUEST;
	protected int priority = 2;
	
	public MessageLanding() {
		super();
	}
	
	public String toString() {
		return "I'm a Landing!";
	}
	
	private static final long serialVersionUID = -7322784783202314394L;
}
