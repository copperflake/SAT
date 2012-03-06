package sat.tower;

import sat.com.Radio;
import sat.crypto.RSAKeyPair;

public class Tower {
	/**
	 * Singleton Constructor
	 */
	private static final Tower instance = new Tower();
	// Private constructor prevents instantiation from other classes
	private Tower() {
		
	}
	public static Tower getInstance() {
		return instance;
	}
	
	/**
	 * Class
	 */
	private Radio radio;
}
