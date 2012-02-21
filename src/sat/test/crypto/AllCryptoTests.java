package sat.test.crypto;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ RSAKeyPairTest.class, RSAKeyTest.class })
public class AllCryptoTests {}
