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

	public static class RadioSocketInputStream extends FilterInputStream {
		public RadioSocketInputStream(InputStream in) {
			super(in);
		}

		public InputStream getStream() {
			return in;
		}

		public void upgrade(InputStream in) {
			this.in = in;
		}
	}

	public static class RadioSocketOutputStream extends FilterOutputStream {
		public RadioSocketOutputStream(OutputStream out) {
			super(out);
		}

		public OutputStream getStream() {
			return out;
		}

		public void upgrade(OutputStream out) {
			this.out = out;
		}
	}
}
