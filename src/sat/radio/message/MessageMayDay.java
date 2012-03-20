package sat.radio.message;

public class MessageMayDay extends Message {
	protected MessageType type = MessageType.MAYDAY;
	protected int priority = 0;

	private String cause;

	public MessageMayDay(String cause) {
		super();
		this.cause = cause;
	}

	public String toString() {
		return "I'm a May Day!";
	}

	private static final long serialVersionUID = -1838109809346574324L;
}
