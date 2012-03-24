package sat.radio.message;

import sat.radio.RadioID;

public class MessageMayDay extends Message {
	private String cause;

	public MessageMayDay(RadioID id, int px, int py, String cause) {
		super(id, px, py);

		this.cause = cause;
	}

	public void resetTypeAndPriority() {
		type = MessageType.MAYDAY;
		priority = 0;
	}

	public String getCause() {
		return cause;
	}

	public String toString() {
		return "I'm a May Day!";
	}

	private static final long serialVersionUID = -1838109809346574324L;
}
