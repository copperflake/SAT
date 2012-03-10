package sat.tower;

import sat.radio.ServerRadio;
import sat.radio.engine.ServerRadioEngine;

public class Tower {
	/**
	 * Singleton Constructor
	 */
	
	private static Tower instance = null;
	
	public static Tower getInstance() {
		if(instance == null)
			instance = new Tower();
		
		return instance;
	}

	/**
	 * Class
	 */
	
	private ServerRadio radio = new ServerRadio();
	
	private Tower() {}
	
	public void listen(ServerRadioEngine engine) {
		radio.listen(engine);
	}
	
	public static void main(String[] args) {
		System.out.println("I'm a tower !");
		
		Tower tower = getInstance();
		
		TowerREPL repl = new TowerREPL(tower, System.in, System.out);
		Thread replThread = repl.runInNewThread();
	}
}
