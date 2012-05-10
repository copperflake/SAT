package sat.radio.message;

import sat.radio.RadioID;
import sat.radio.RadioProtocolException;
import sat.radio.message.handler.MessageHandler;
import sat.utils.geo.Coordinates;

public class MessageRouting extends Message {

	public MessageRouting(RadioID id, Coordinates c) {
		super(id, c);
	}

	public void setTypeAndPriority() {
		type = MessageType.ROUTING;
		priority = 2;
	}

	public String toString() {
		return "I'm a routing!";
	}

	public void handle(MessageHandler handler) throws RadioProtocolException {
		handler.handle(this);
	}

	private static final long serialVersionUID = -8493801549147991470L;
}
