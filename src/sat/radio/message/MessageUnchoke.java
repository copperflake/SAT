package sat.radio.message;

public class MessageUnchoke extends Message {
	protected MessageType type = MessageType.UNCHOKE;
	protected int priority = 4;

	public MessageUnchoke() {
		super();
	}

	public String toString() {
		return "I'm a Unchoke!";
	}

	private static final long serialVersionUID = -4086684315280907680L;
}
