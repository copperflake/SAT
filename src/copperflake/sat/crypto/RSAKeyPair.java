package copperflake.sat.crypto;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;

public final class RSAKeyPair {
	private RSAKey publicKey;
	private RSAKey privateKey;
	
	public RSAKeyPair() {
		this(128);
	}
	
	public RSAKeyPair(int keyLength) throws RSAKeyTooShortException {
		if(keyLength < 128)
			throw new RSAKeyTooShortException();
		
		Random rand = new SecureRandom();
		
		BigInteger e = BigInteger.valueOf(65537);
		BigInteger p, q, n, phi, d;
		
		do {
			// Les nombres devraient différer de quelques digits afin d'éviter un certain type d'attaque.
			p = BigInteger.probablePrime(keyLength/2, rand);
			q = BigInteger.probablePrime(keyLength/2, rand);
			n = p.multiply(q);
			
			phi = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE)); // (p-1)(q-1)
		} while(phi.compareTo(e) != 1);
		
		d = e.modInverse(phi);
		
		this.publicKey = new RSAKey(e, n);
		this.privateKey = new RSAKey(d, n);
	}
	
	public RSAKeyPair(RSAKey publicKey) {
		this.publicKey = publicKey;
		this.privateKey = null;
	}
	
	public RSAKeyPair(RSAKey publicKey, RSAKey privateKey) {
		// TODO: check key validity
	}
	
	public RSAKey getPrivateKey() throws RSANoPrivateKeyException {
		if(privateKey == null)
			throw new RSANoPrivateKeyException();
		
		return privateKey;
	}
	
	public RSAKey getPublicKey() throws RSANoPrivateKeyException {
		return publicKey;
	}
	
	public BigInteger encrypt(BigInteger m) { return BigInteger.ONE; }
	public BigInteger decrypt(BigInteger m) { return BigInteger.ONE; }
	public BigInteger sign(BigInteger m) { return this.decrypt(m); }
	public BigInteger unsign(BigInteger m) { return this.encrypt(m); }
}
