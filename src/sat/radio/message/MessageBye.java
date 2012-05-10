package sat.radio.message;

import sat.radio.RadioID;
import sat.radio.RadioProtocolException;
import sat.radio.message.handler.MessageHandler;
import sat.utils.geo.Coordinates;

public class MessageBye extends Message {
	public MessageBye(RadioID id, Coordinates c) {
		super(id, c);
	}

	public void setTypeAndPriority() {
		type = MessageType.BYE;
		priority = 4;
	}

	public String toString() {
		return "I'm a Bye!";
	}

	public void handle(MessageHandler handler) throws RadioProtocolException {
		handler.handle(this);
	}

	private static final long serialVersionUID = 421645651039774637L;
}
