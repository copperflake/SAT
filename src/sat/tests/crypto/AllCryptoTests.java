package sat.tests.crypto;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Execute tous les tests cryptographiques.
 */
@RunWith(Suite.class)
@SuiteClasses({ RSAKeyPairTest.class, RSAKeyTest.class })
public class AllCryptoTests {
}
