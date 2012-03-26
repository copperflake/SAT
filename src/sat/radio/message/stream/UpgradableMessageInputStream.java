package sat.radio.message.stream;

import java.io.IOException;
import java.io.InputStream;

import sat.radio.message.Message;

public class UpgradableMessageInputStream extends MessageInputStream {
	private MessageInputStream messages_in;

	public UpgradableMessageInputStream(InputStream in) {
		super(in);
		messages_in = new LegacyMessageInputStream(in);
	}

	public Message readMessage() throws IOException {
		return messages_in.readMessage();
	}

	public void upgrade() throws IOException {
		messages_in = new ExtendedMessageInputStream(in);
	}
}
