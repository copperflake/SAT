package sat.tower;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import sat.GlobalCLI;
import sat.radio.engine.RadioServerEngine;
import sat.radio.engine.RadioServerFileEngine;
import sat.radio.engine.RadioServerTCPEngine;
import sat.radio.engine.RadioServerUDPEngine;

public class TowerCLI extends GlobalCLI {
	private Tower tower;
	
	public TowerCLI(Tower tower, InputStream in, PrintStream out) {
		super(in, out, "Tower> ");
		this.tower = tower;
	}
	
	public void listen(String engineType, String arg) throws IOException {
		RadioServerEngine engine;
		
		if(engineType.equals("file")) {
			out.println("[Warning] `listen file` requires a *nix system and is depreciated!");
			engine = new RadioServerFileEngine(arg);
		} else if(engineType.equals("tcp")) {
			int port = Integer.parseInt(arg);
			engine = new RadioServerTCPEngine(port);
		} else if(engineType.equals("udp")) {
			int port = Integer.parseInt(arg);
			engine = new RadioServerUDPEngine(port);
		} else {
			out.println("Error: unknown radio engine type");
			return;
		}
		
		tower.listen(engine);
	}
}
