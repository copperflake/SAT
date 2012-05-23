package sat.radio.message;

import sat.radio.RadioID;
import sat.utils.geo.Coordinates;

public class MessageMayDay extends Message {
	private String cause;
	private int length;

	public MessageMayDay(RadioID id, Coordinates c, String cause) {
		super(id, c);

		type = MessageType.MAYDAY;
		priority = 0;

		this.cause = cause;
		this.length = cause.getBytes().length;
	}

	public String getCause() {
		return cause;
	}

	public String toString() {
		return "I'm a May Day!";
	}

	private static final long serialVersionUID = -1838109809346574324L;

	public int getLength() {
		return length;
	}
}
