package sat.crypto;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;

public class RSAInputStream extends InputStream {
    protected InputStream in;
    protected RSAKeyPair keys;
    
    protected int blockSize;
    
    protected int bufferSize;
    protected int bufferLength = 0;
    protected int bufferPosition = 0;
    protected byte[] buffer;
    
    public RSAInputStream(InputStream in, RSAKeyPair keys) {
        this.in = in;
        this.keys = keys;
        
        blockSize = keys.keyLength() / 8;
        bufferSize = blockSize - 2;
        buffer = new byte[bufferSize];
    }
    
    public int available() throws IOException {
        return bufferLength-bufferPosition;
    }
    
    protected void load() throws IOException {
    	byte[] block = new byte[blockSize+1]; // Implicit 0 front byte
    	
        for(int i = 1; i <= blockSize; i++) {
            block[i] = (byte) (in.read() & 0xff); 
        }
        
        block = keys.decrypt(new BigInteger(block)).toByteArray();
        
        // BigInteger outputs:
        //
        //    [-----key----] -> l = n
        //
        // 1) [0|pad|0|buff] -> l = n
        // 2)   [pad|0|buff] -> l = n-1
		// 3)   [0|--buff--] -> l = n-2
        // 4)     [--buff--] -> l = n-3
        
        int drop = 0, bufferOffset = -1;
        switch(blockSize-block.length) {
        	case 0:
        		// case 1, then case 2...
        		drop = 1;
        		
        	case 1:
        		// Padding cannot be '0'
        		if(block[drop] != 0) {
        			// case 2
        			for(int i = 1; i < block.length; i++) {
        				if(block[drop+i] == 0) {
        					bufferOffset = drop+i+1;
        					break;
        				}
        			}
        		} else {
        			// case 3
        			bufferOffset = 1;
        		}
        		break;
        	
        	case 2:
        		// case 4
        		bufferOffset = 0;
        		break;
        }
        
        // No-match -> bad block
        if(bufferOffset < 0)
        	throw new IOException("Invalid RSA block");
        
        // Buffer-reset
        bufferLength = block.length-bufferOffset;
        bufferPosition = 0;
        
        // Copy bytes
        System.arraycopy(block, bufferOffset, buffer, 0, bufferLength);
    }
    
    public int read() throws IOException {
        if(bufferPosition >= bufferLength)
            load();
        
        return buffer[bufferPosition++];
    }
}