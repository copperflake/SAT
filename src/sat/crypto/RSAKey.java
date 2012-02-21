package sat.crypto;

import java.math.BigInteger;

public class RSAKey {
	private BigInteger exponent;
	private BigInteger modulus;
	
	public RSAKey(BigInteger e, BigInteger m) {
		exponent = e;
		modulus = m;
	}

	public BigInteger getExponent() {
		return exponent;
	}

	public BigInteger getModulus() {
		return modulus;
	}
	
	public String toString() {
		return "(e:"+exponent+";m:"+modulus+")";
	}
}
