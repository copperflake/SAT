package sat.radio.message;

import sat.radio.RadioID;
import sat.radio.RadioProtocolException;
import sat.radio.message.handler.MessageHandler;
import sat.utils.geo.Coordinates;

public class MessageUnchoke extends Message {
	public MessageUnchoke(RadioID id, Coordinates c) {
		super(id, c);
	}

	public void setTypeAndPriority() {
		type = MessageType.UNCHOKE;
		priority = 4;
	}

	public String toString() {
		return "I'm a Unchoke!";
	}

	public void handle(MessageHandler handler) throws RadioProtocolException {
		handler.handle(this);
	}

	private static final long serialVersionUID = -4086684315280907680L;
}
