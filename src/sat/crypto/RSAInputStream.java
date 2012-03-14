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
        
        blockSize = (keys.keyLength() - 1) / 8;
        bufferSize = blockSize - 2;
        buffer = new byte[bufferSize];
    }
    
    public int available() throws IOException {
        return bufferLength-bufferPosition;
    }
    
    protected void load() throws IOException {
        byte[] block = new byte[blockSize+1];
        
        for(int i = 0; i < blockSize+1; i++) {
            block[i] = (byte) (in.read() & 0xff); 
        }
        
        BigInteger block_bigint = new BigInteger(block);
        block_bigint = keys.decrypt(block_bigint);

        //System.out.println("<" + block_bigint);
        
        block = block_bigint.toByteArray();
        
        boolean padding = true;
        int j = 0;
        for(int i = 1; i < block.length; i++) {
            if(padding) {
                if(block[i] == 0) {
                    padding = false;
                }
            } else {
                buffer[j++] = block[i];
            }
        }
        
        bufferLength = j;
        bufferPosition = 0;
    }
    
    public int read() throws IOException {
        if(bufferPosition >= bufferLength)
            load();
        
        return buffer[bufferPosition++];
    }
}