package sat.tests.crypto;

import static org.junit.Assert.*;

import java.math.BigInteger;

import org.junit.Test;

/**
 * Test de l'objet RSAKey.
 */
public class RSAKeyTest {
	/**
	 * Vérifie qu'une clé créée avec un exposant et un module spécifique
	 * retourne bien ce module et cet exposant.
	 */
	@Test
	public void test() {
		sat.utils.crypto.RSAKey key = new sat.utils.crypto.RSAKey(BigInteger.ONE, BigInteger.TEN);

		assertEquals(BigInteger.ONE, key.getExponent());
		assertEquals(BigInteger.TEN, key.getModulus());
	}
}
