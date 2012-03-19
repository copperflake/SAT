package sat.tower;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.InetAddress;

import sat.GlobalCLI;
import sat.radio.engine.server.RadioServerEngine;
import sat.radio.engine.server.RadioServerFileEngine;
import sat.radio.engine.server.RadioServerTCPEngine;

public class TowerCLI extends GlobalCLI {
	private Tower tower;
	
	public TowerCLI(Tower tower, InputStream in, PrintStream out) {
		super(in, out, "Tower> ");
		this.tower = tower;
	}
	
	public void listen(String engineType, String arg1, String arg2) throws IOException {
		RadioServerEngine engine;
		
		if(engineType.isEmpty()) {
			engineType = "tcp";
		}
		
		if(engineType.equals("file")) {
			out.println("[Warning] `listen file` requires a *nix system and is depreciated!");
			engine = new RadioServerFileEngine(arg1);
		} else if(engineType.equals("tcp")) {
			if(arg1.isEmpty()) arg1 = "6969"; // Default port
			int port = Integer.parseInt(arg1);
			if(!arg2.isEmpty()) {
				InetAddress iface = InetAddress.getByName(arg2);
				engine = new RadioServerTCPEngine(port, iface);
			} else {
				engine = new RadioServerTCPEngine(port);
			}
		} else {
			out.println("Error: unknown radio engine type");
			return;
		}
		
		tower.listen(engine);
	}
}
