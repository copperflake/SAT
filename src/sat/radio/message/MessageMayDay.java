package sat.radio.message;

import sat.radio.RadioID;
import sat.utils.geo.Coordinates;

@SuppressWarnings("serial")
public class MessageMayDay extends Message {
	private String cause;
	
	public MessageMayDay(RadioID id, Coordinates c, String cause) {
		super(id, c);

		type = MessageType.MAYDAY;
		priority = 0;

		this.cause = cause;
		length = cause.getBytes().length;
	}

	public String getCause() {
		return cause;
	}

	public String toString() {
		return "I'm a May Day!";
	}
}
