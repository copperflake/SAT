package sat.radio.message.stream;

import java.io.IOException;
import java.io.OutputStream;

import sat.radio.message.Message;

public class UpgradableMessageOutputStream extends MessageOutputStream {
	private MessageOutputStream messages_out;

	public UpgradableMessageOutputStream(OutputStream out) {
		super(out);
		messages_out = new LegacyMessageOutputStream(out);
	}

	public void writeMessage(Message m) throws IOException {
		messages_out.writeMessage(m);
	}

	public void upgrade() throws IOException {
		messages_out = new ExtendedMessageOutputStream(out);
	}
}
