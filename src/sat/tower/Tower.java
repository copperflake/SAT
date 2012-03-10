package sat.tower;

/*import sat.com.Radio;
import sat.crypto.RSAKeyPair;*/

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
	
	public static void main(String[] args) {
		System.out.println("I'm a tower !");
		Tower tower = getInstance();
		TowerREPL repl = new TowerREPL(tower, System.in, System.out);
		Thread replThread = repl.runInNewThread();
	}
	
	/**
	 * Class
	 */
	//private Radio radio;
}
