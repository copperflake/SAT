package sat.utils.crypto;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;

import sat.EndOfWorldException;

/**
 * Une paire de clé RSA (publique, privée). Cette classe fourni également les
 * méthodes de chiffrement et déchiffrement avec ces clés.
 * <p>
 * Il est possible d'avoir un demi-paire de clé, c'est à dire une paire de clé
 * dont la composante privée est absente. Dans un tel cas, il n'est pas possible
 * d'utiliser les fonctions nécessitant une clé privée. La clé publique est en
 * revanche toujours disponible.
 */
public final class RSAKeyPair {
	/**
	 * La clé publique.
	 */
	private RSAKey publicKey;

	/**
	 * La clé privée. Possiblement null.
	 */
	private RSAKey privateKey;

	/**
	 * La longueur des clés de cette paire de clé.
	 */
	private int keyLength;

	/**
	 * Crée une nouvelle paire de clé avec une clé publique et privée générée
	 * aléatoirement. La longueur de ces clés sera de 1024 bits.
	 */
	public RSAKeyPair() {
		try {
			generateKeyPair(1024);
			this.keyLength = 1024;
		}
		catch(RSAException e) {
			throw new EndOfWorldException();
		}
	}

	/**
	 * Crée une paire de clé générée dynamiquement de taille donnée.
	 * 
	 * @param keyLength
	 *            La longeueur des clés générées.
	 * 
	 * @throws RSAException
	 *             Si la longueur des clés est insufisante (< 128 bits).
	 */
	public RSAKeyPair(int keyLength) throws RSAException {
		generateKeyPair(keyLength);
		this.keyLength = keyLength;
	}

	/**
	 * Crée une demi-paire de clé avec uniquement une composante publique.
	 * 
	 * @param publicKey
	 *            La composante publique de cette paire de clé.
	 */
	public RSAKeyPair(RSAKey publicKey) {
		this.publicKey = publicKey;
		this.privateKey = null; // Half keyPair

		this.keyLength = publicKey.getLength();
	}

	/**
	 * Crée une paire de clé contenant une composante privée et publique donnée.
	 * 
	 * @param publicKey
	 *            La composante publique de la paire de clé.
	 * @param privateKey
	 *            La composante privée de la paire de clé.
	 * 
	 * @throws RSAException
	 *             Si les deux clés fournies sont incompatible entre elles, une
	 *             exception est générée.
	 */
	public RSAKeyPair(RSAKey publicKey, RSAKey privateKey) throws RSAException {
		// TODO: check key validity (better)
		if(!publicKey.getModulus().equals(privateKey.getModulus()))
			throw new RSAException("Invalid key pair");

		if(publicKey.getLength() != privateKey.getLength())
			throw new RSAException("Invalid key pair");

		if(publicKey.getLength() % 8 != 0)
			throw new RSAException("Invalid key pair");

		this.publicKey = publicKey;
		this.privateKey = privateKey;

		this.keyLength = publicKey.getLength();
	}

	/**
	 * Fonction interne de génération de clé.
	 */
	private void generateKeyPair(int keyLength) throws RSAException {
		if(keyLength < 128)
			throw new RSAException("Key is too short");

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
				}
				while(p.equals(q)); // p != q

				n = p.multiply(q); // RSA: n = pq
			}
			while(n.bitLength() != keyLength);

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
		}
		// e < [φ/λ] & e premier à λ
		while(lambda.compareTo(e) != 1 || !e.gcd(lambda).equals(BigInteger.ONE));

		// e est premier à λ, donc d existe
		d = e.modInverse(lambda);

		this.publicKey = new RSAKey(e, n);
		this.privateKey = new RSAKey(d, n);
	}

	/**
	 * Retourne la clé privée de cette paire de clé.
	 * 
	 * @throws RSAException Si la clé privée n'est pas disponible.
	 */
	public RSAKey getPrivateKey() throws RSAException {
		if(privateKey == null)
			throw new RSAException("Private key is missing");

		return privateKey;
	}

	/**
	 * Retourne la clé publique de cette paire de clé. Contrairement à la clé préviée, celle-ci est toujours disponible.
	 */
	public RSAKey getPublicKey() {
		return publicKey;
	}

	/**
	 * Retourne la longueur des clés de cette paire de clé.
	 */
	public int keyLength() {
		return keyLength;
	}

	/**
	 * Crée une nouvelle paire de clé dont la composante privée est absente.
	 */
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
	 * KeyPair string serialization. [DEBUG]
	 */
	public String toString() {
		return "{" + publicKey + ";" + privateKey + "}";
	}
}
