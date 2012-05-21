package sat.radio.message;

import sat.radio.RadioID;
import sat.utils.geo.Coordinates;

public class MessageMayDay extends Message {
	private String cause;

	public MessageMayDay(RadioID id, Coordinates c, String cause) {
		super(id, c);

		type = MessageType.MAYDAY;
		priority = 0;

		this.cause = cause;
	}

	public String getCause() {
		return cause;
	}

	public String toString() {
		return "I'm a May Day!";
	}

	private static final long serialVersionUID = -1838109809346574324L;
}
