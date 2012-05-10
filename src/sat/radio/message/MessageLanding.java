package sat.radio.message;

import sat.radio.RadioID;
import sat.radio.RadioProtocolException;
import sat.radio.message.handler.MessageHandler;
import sat.utils.geo.Coordinates;

public class MessageLanding extends Message {
	public MessageLanding(RadioID id, Coordinates c) {
		super(id, c);
	}

	public void setTypeAndPriority() {
		type = MessageType.LANDINGREQUEST;
		priority = 2;
	}

	public String toString() {
		return "I'm a Landing!";
	}

	public void handle(MessageHandler handler) throws RadioProtocolException {
		handler.handle(this);
	}

	private static final long serialVersionUID = -7322784783202314394L;
}
