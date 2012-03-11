package sat.radio;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class RadioSocket {
	public InputStream in;
	public OutputStream out;

	public InputStream engineIn;
	public OutputStream engineOut;
	
	public class RadioSocketInput extends InputStream {
		public int read() throws IOException {
			// TODO Auto-generated method stub
			return 0;
		}
	}
}
