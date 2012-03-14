package sat.crypto;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;

public class RSAOutputStream extends OutputStream {
	protected OutputStream out;
	protected RSAKeyPair keys;
	
	protected int blockSize;
	protected int bufferSize;
	protected int bufferLength = 0;
	protected byte[] buffer;
	
	public RSAOutputStream(OutputStream out, RSAKeyPair keys) {
		this.out = out;
		this.keys = keys;
		
		blockSize = keys.keyLength() / 8;
		bufferSize = blockSize - 2;
		buffer = new byte[bufferSize];
	}
	
	public void write(int b) throws IOException {
		buffer[bufferLength++] = (byte) (b & 0xff);
		if(bufferLength >= bufferSize)
			flush();
	}
	
	public void flush() throws IOException {
		if(bufferLength == 0) return;
		
		byte[] block = new byte[blockSize];
		
		//block[0] = 0;
		
		int padding = bufferSize-bufferLength;
		for(int i = 1; i <= padding; i++) {
			block[i] = 123;
		}
		
		//block[padding+1] = 0;
				
		for(int i = 0; i < bufferLength; i++) {
			block[i+padding+2] = buffer[i];
		}
		
		BigInteger block_bigint = new BigInteger(block);
		BigInteger block_bigint_encrypted = keys.encrypt(block_bigint);
		
		byte[] block_encrypted = block_bigint_encrypted.toByteArray();
		
		int drop = (block_encrypted.length > 128) ? 1 : 0;
		int packet_padding = block.length-(block_encrypted.length);
		
		for(int i = 0; i < block.length; i++) {
			if(i < packet_padding) {
				block[i] = 0;
			} else {
				block[i] = block_encrypted[(i-padding)+drop];
			}
		}
		
		out.write(block);
		out.flush();
		
		bufferLength = 0;
	}
	
	public void close() throws IOException {
		flush();
		out.close();
	}
}
