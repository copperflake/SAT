package sat.radio;

import java.io.InputStream;
import java.io.OutputStream;

public class RadioSocketDirect extends RadioSocket {
	public RadioSocketDirect(InputStream in, OutputStream out) {
		this.in = in;
		this.out = out;
	}
}
