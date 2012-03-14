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
		
		blockSize = (keys.keyLength() - 1) / 8;
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
		
		byte[] block = new byte[blockSize+1];
		
		int padding = blockSize-1-bufferLength;
		for(int i = 0; i < padding; i++) {
			block[i+1] = 1;
		}
		
		for(int i = 0; i < bufferLength; i++) {
			block[i+padding+2] = buffer[i];
		}
		
		BigInteger block_bigint = new BigInteger(block);
		
		//System.out.println(">" + block_bigint);
		
		block_bigint = keys.encrypt(block_bigint);
		
		byte[] block_encrypted = block_bigint.toByteArray();
		padding = block.length-block_encrypted.length;
		for(int i = 0; i < block.length; i++) {
			if(i < padding) {
				block[i] = 0;
			} else {
				block[i] = block_encrypted[i-padding];
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
