package sat.radio.socket;

import java.io.InputStream;
import java.io.OutputStream;

public class RadioSocketDirect extends RadioSocket {
	public RadioSocketDirect(InputStream in, OutputStream out) {
		this.in = new RadioSocketInputStream(in);
		this.out = new RadioSocketOutputStream(out);
	}
}
