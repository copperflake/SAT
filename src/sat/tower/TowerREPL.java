package sat.tower;

import java.io.InputStream;
import java.io.PrintStream;
import java.net.InetAddress;

import sat.radio.engine.RadioServerEngine;
import sat.radio.engine.RadioServerFileEngine;
import sat.radio.engine.RadioServerTCPEngine;
import sat.radio.engine.RadioServerUDPEngine;
import sat.repl.REPL;

public class TowerREPL extends REPL {
	private Tower tower;
	
	public TowerREPL(Tower tower, InputStream in, PrintStream out) {
		super(in, out, "Tower> ");
		this.tower = tower;
	}
	
	public void listen(String engineType, String arg) {
		RadioServerEngine engine;
		
		if(engineType.equals("file")) {
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
	
	public void exit() {
		super.exit();
	}
}
