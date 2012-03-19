package sat.tower;

import java.io.IOException;

import sat.radio.RadioServer;
import sat.radio.engine.server.RadioServerEngine;
import sat.radio.RadioServerDelegate;

public class Tower implements RadioServerDelegate {
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
	
	public void listen(RadioServerEngine engine) throws IOException {
		if(radio == null) radio = new RadioServer(this);
		radio.listen(engine);
	}
	
	public static void main(String[] args) {
		System.out.println("I'm a tower !");
		
		Tower tower = getInstance();
		
		TowerCLI repl = new TowerCLI(tower, System.in, System.out);
		Thread replThread = repl.runInNewThread();
	}
}
