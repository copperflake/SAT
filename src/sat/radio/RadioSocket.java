package sat.radio;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class RadioSocket {
	protected RadioID id;
	
	public InputStream in;
	public OutputStream out;
	
	public void close() throws IOException {
		in.close();
		out.close();
	}
}
