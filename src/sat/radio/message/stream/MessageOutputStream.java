package sat.radio.message.stream;

import java.io.FilterOutputStream;
import java.io.OutputStream;

public class MessageOutputStream extends FilterOutputStream {
	public MessageOutputStream(OutputStream out) {
		super(out);
	}
}
