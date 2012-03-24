package sat.radio.socket;

import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import sat.radio.RadioID;

public abstract class RadioSocket {
	protected RadioID id;

	public RadioSocketInputStream in;
	public RadioSocketOutputStream out;

	public void close() throws IOException {
		in.close();
		out.close();
	}

	protected class RadioSocketInputStream extends FilterInputStream {
		public RadioSocketInputStream(InputStream in) {
			super(in);
		}

		public void upgrade(InputStream in) {
			this.in = in;
		}
	}

	protected class RadioSocketOutputStream extends FilterOutputStream {
		public RadioSocketOutputStream(OutputStream out) {
			super(out);
		}

		public void upgrade(OutputStream out) {
			this.out = out;
		}
	}
}
