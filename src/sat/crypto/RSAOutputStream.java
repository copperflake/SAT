package sat.crypto;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class RSAOutputStream extends FilterOutputStream {
	public RSAOutputStream(OutputStream out) {
		super(out);
	}
	
	public void write(int b) throws IOException {
		out.write(b);
	}
}
