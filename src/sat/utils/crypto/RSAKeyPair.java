package sat.utils.crypto;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;

import sat.EndOfWorldException;

public final class RSAKeyPair {
	private RSAKey publicKey;
	private RSAKey privateKey;
	private int keyLength;

	public RSAKeyPair() {
		try {
			generateKeyPair(1024);
			this.keyLength = 1024;
		} catch(RSAKeyTooShortException e) {
			throw new EndOfWorldException("keyLength of 1024 is not too small!");
		}
	}

	public RSAKeyPair(int keyLength) throws RSAKeyTooShortException {
		generateKeyPair(keyLength);
		this.keyLength = keyLength;
	}

	public RSAKeyPair(RSAKey publicKey) {
		this.publicKey = publicKey;
		this.privateKey = null; // Half keyPair

		this.keyLength = publicKey.getLength();
	}

	public RSAKeyPair(RSAKey publicKey, RSAKey privateKey) throws RSAInvalidKeyPairException {
		// TODO: check key validity (better)
		if(!publicKey.getModulus().equals(privateKey.getModulus()))
			throw new RSAInvalidKeyPairException();

		if(publicKey.getLength() != privateKey.getLength())
			throw new RSAInvalidKeyPairException();

		if(publicKey.getLength() % 8 != 0)
			throw new RSAInvalidKeyPairException();

		this.publicKey = publicKey;
		this.privateKey = privateKey;

		this.keyLength = publicKey.getLength();
	}

	private void generateKeyPair(int keyLength) throws RSAKeyTooShortException {
		if(keyLength < 128)
			throw new RSAKeyTooShortException();

		if(keyLength % 8 != 0)
			keyLength += (keyLength % 8);

		Random rand = new SecureRandom();

		BigInteger e = new BigInteger("65537"); // e est premier
		BigInteger n, phi, lambda, d;

		do {
			BigInteger p, q;

			do {
				do {
					// TODO: Les nombres devraient différer de quelques digits afin
					// d'éviter un certain type d'attaque.
					p = BigInteger.probablePrime(keyLength / 2, rand);
					q = BigInteger.probablePrime(keyLength / 2, rand);
				} while(p.equals(q)); // p != q

				n = p.multiply(q); // RSA: n = pq
			} while(n.bitLength() != keyLength);

			BigInteger pMin1 = p.subtract(BigInteger.ONE); // (p-1)
			BigInteger qMin1 = q.subtract(BigInteger.ONE); // (q-1)

			phi = pMin1.multiply(qMin1); // RSA: φ(n) = (p-1)(q-1)

			// Using λ(n) instead of φ(n):
			// - The original version of RSA defined φ(n) = (p-1)(q-1).
			// - In fact you can use the smaller Charmichael function instead:
			//   λ(n) = [(p-1)(q-1)]/gcd(p-1, q-1).
			// - Later refinements of the RSA algorithm like PKCS#1 use 
			//   this definition.
			lambda = phi.divide(pMin1.gcd(qMin1));
		} while(lambda.compareTo(e) != 1 || !e.gcd(lambda).equals(BigInteger.ONE)); // e < [φ/λ] & e premier à λ

		// e est premier à λ, donc d existe
		d = e.modInverse(lambda);

		this.publicKey = new RSAKey(e, n);
		this.privateKey = new RSAKey(d, n);
	}

	public RSAKey getPrivateKey() throws RSAMissingPrivateKeyException {
		if(privateKey == null)
			throw new RSAMissingPrivateKeyException();

		return privateKey;
	}

	public RSAKey getPublicKey() {
		return publicKey;
	}

	public int keyLength() {
		return keyLength;
	}

	public RSAKeyPair makePublic() {
		return new RSAKeyPair(getPublicKey());
	}

	public BigInteger encrypt(BigInteger m) {
		return m.modPow(publicKey.getExponent(), publicKey.getModulus());
	}

	public BigInteger decrypt(BigInteger m) {
		return m.modPow(privateKey.getExponent(), privateKey.getModulus());
	}

	// Aliases for signing
	public BigInteger sign(BigInteger m) {
		return this.decrypt(m);
	}

	public BigInteger unsign(BigInteger m) {
		return this.encrypt(m);
	}

	/**
	 * KeyPair string serialization
	 */
	public String toString() {
		return "{" + publicKey + ";" + privateKey + "}";
	}
}
