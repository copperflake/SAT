package sat.test.crypto;

import static org.junit.Assert.*;

import java.math.BigInteger;

import org.junit.Test;

public class RSAKeyTest {
	@Test
	public void test() {
		sat.crypto.RSAKey key = new sat.crypto.RSAKey(BigInteger.ONE, BigInteger.TEN);
		
		assertEquals(BigInteger.ONE, key.getExponent());
		assertEquals(BigInteger.TEN, key.getModulus());
	}
}
