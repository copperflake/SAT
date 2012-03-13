package sat.tower;

import java.io.IOException;

import sat.radio.RadioServer;
import sat.radio.engine.server.RadioServerEngine;
import sat.radio.RadioServerDelegate;

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
	
	private RadioServer radio = null;
	
	private Tower() {}
	
	public boolean listen(RadioServerEngine engine) throws IOException {
		if(radio != null) return false;
		
		radio = new RadioServer(new RadioServerDelegate() {
			
		});
		
		radio.listen(engine);
		
		return true;
	}
	
	public static void main(String[] args) {
		System.out.println("I'm a tower !");
		
		Tower tower = getInstance();
		
		TowerCLI repl = new TowerCLI(tower, System.in, System.out);
		Thread replThread = repl.runInNewThread();
	}
}
