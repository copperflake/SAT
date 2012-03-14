package sat.crypto;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class RSAInputStream extends FilterInputStream {
	public RSAInputStream(InputStream in) {
		super(in);
	}
	
	public int read() throws IOException {
		int b = in.read() & 0xff;
		return b;
	}
}
