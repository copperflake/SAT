package sat.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import sat.tests.crypto.AllCryptoTests;
import sat.tests.radio.AllRadioTests;

@RunWith(Suite.class)
@SuiteClasses({ AllCryptoTests.class, AllRadioTests.class })
public class AllTests {
}
