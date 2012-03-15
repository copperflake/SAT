package sat.crypto;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;

public class RSAOutputStream extends OutputStream {
    protected OutputStream out;
    protected RSAKeyPair keys;
    
    protected int blockSize;
    protected byte[] block;
    
    protected int bufferSize;
    protected int bufferLength = 0;
    protected byte[] buffer;
    
    protected Random rand = null;
    
    public RSAOutputStream(OutputStream out, RSAKeyPair keys) {
        this.out = out;
        this.keys = keys;
        
        blockSize = keys.keyLength() / 8;
        bufferSize = blockSize - 2;
        
        buffer = new byte[bufferSize];
        
        // Only one allocation (no zero-ing on flush)
        block = new byte[blockSize];
    }
    
    public void write(int b) throws IOException {
        buffer[bufferLength++] = (byte) (b & 0xff);
        if(bufferLength >= bufferSize)
            flush();
    }
    
    public void flush() throws IOException {
        if(bufferLength == 0) return;
        
        // Front zero
        block[0] = 0;
        
        // Padding with random bytes
        int padding = (blockSize-2)-bufferLength;
        if(padding > 0) {
        	// Lazy allocation for SecureRandom
        	if(rand == null)
        		rand = new SecureRandom();
        	
        	byte[] padding_bytes = new byte[padding];
        	rand.nextBytes(padding_bytes);
        	
        	for(int i = 0; i < padding_bytes.length; i++) {
        		// Check zero-byte
        		while(padding_bytes[i] == 0) {
    				padding_bytes[i] = (byte) (rand.nextInt() & 0xff);
    			}
        	}
        	
        	System.arraycopy(padding_bytes, 0, block, 1, padding);
        }
        
        // Padding boundary
        block[padding+1] = 0;
        
        // Buffer copying
        System.arraycopy(buffer, 0, block, padding+2, bufferLength);
        
        // Encrypt
        byte[] block_encrypted = keys.encrypt(new BigInteger(block)).toByteArray();
        
        int drop = 0;
        if(block_encrypted.length > block.length) {
        	drop = 1;
        	padding = 0;
        } else {
        	drop = 0;
        	padding = block.length-block_encrypted.length;
        }
        
        for(int i = 0; i < padding; i++)
            block[i] = 0;
        
        System.arraycopy(block_encrypted, drop, block, padding, block_encrypted.length-drop);
        
        out.write(block);
        out.flush();
        
        bufferLength = 0;
    }
    
    public void close() throws IOException {
        flush();
        out.close();
    }
}