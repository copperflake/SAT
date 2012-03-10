package sat.tower;

public class Tower {
	/**
	 * Singleton Constructor
	 */
	private static Tower instance = null;
	
	private Tower() {}
	
	public static Tower getInstance() {
		if(instance == null)
			instance = new Tower();
		
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
