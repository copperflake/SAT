package sat.crypto;

import java.math.BigInteger;

/**
 * Clé de chiffrement RSA.
 */
public class RSAKey {
	/**
	 * Exposant de la clé.
	 */
	private BigInteger exponent;

	/**
	 * Modulus de la clé.
	 */
	private BigInteger modulus;

	/**
	 * La longueur de la clé (c'est à dire la longueur du modulo).
	 */
	private int length;

	/**
	 * Crée une clé RSA avec un exposant <code>e</code> et un modulus
	 * <code>m</code>.
	 * 
	 * @param e
	 *            L'exposant de la clé
	 * @param m
	 *            Le modulus de la clé
	 */
	public RSAKey(BigInteger e, BigInteger m) {
		// TODO: checks components validity

		exponent = e;
		modulus = m;

		length = modulus.bitLength();
	}

	/**
	 * Retourne l'exposant de la clé.
	 */
	public BigInteger getExponent() {
		return exponent;
	}

	/**
	 * Retourne le modulus de la clé.
	 */
	public BigInteger getModulus() {
		return modulus;
	}

	/**
	 * Retourne la longueur de la clé (c'est à dire la longueur du modulus).
	 */
	public int getLength() {
		return length;
	}
}
