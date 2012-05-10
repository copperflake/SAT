package sat.radio.message;

import sat.radio.RadioID;
import sat.radio.RadioProtocolException;
import sat.radio.message.handler.MessageHandler;
import sat.utils.geo.Coordinates;

public class MessageMayDay extends Message {
	private String cause;

	public MessageMayDay(RadioID id, Coordinates c, String cause) {
		super(id, c);

		this.cause = cause;
	}

	public void setTypeAndPriority() {
		type = MessageType.MAYDAY;
		priority = 0;
	}

	public String getCause() {
		return cause;
	}

	public String toString() {
		return "I'm a May Day!";
	}

	public void handle(MessageHandler handler) throws RadioProtocolException {
		handler.handle(this);
	}

	private static final long serialVersionUID = -1838109809346574324L;
}
