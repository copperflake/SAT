package sat.radio.message;

import sat.radio.RadioID;

public class MessageMayDay extends Message {
	private String cause;

	public MessageMayDay(RadioID id, int px, int py, String cause) {
		super(id, px, py);

		type = MessageType.MAYDAY;
		priority = 0;

		this.cause = cause;
	}

	public String toString() {
		return "I'm a May Day!";
	}

	private static final long serialVersionUID = -1838109809346574324L;
}
