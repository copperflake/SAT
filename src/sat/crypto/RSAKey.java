package sat.crypto;

import java.math.BigInteger;

public class RSAKey {
	private BigInteger exponent;
	private BigInteger modulus;
	private int length;
	
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
	
	public int length() {
		return length;
	}
	
	public String toString() {
		return "(e:"+exponent+";m:"+modulus+")";
	}
}
