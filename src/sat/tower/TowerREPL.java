package sat.tower;

import java.io.InputStream;
import java.io.PrintStream;
import java.net.InetAddress;

import sat.radio.engine.ServerRadioEngine;
import sat.radio.engine.ServerRadioFileEngine;
import sat.radio.engine.ServerRadioTCPEngine;
import sat.radio.engine.ServerRadioUDPEngine;
import sat.repl.REPL;

public class TowerREPL extends REPL {
	private Tower tower;
	
	public TowerREPL(Tower tower, InputStream in, PrintStream out) {
		super(in, out, "Tower> ");
		this.tower = tower;
	}
	
	public void listen(String engineType, String arg) {
		ServerRadioEngine engine;
		
		if(engineType.equals("file")) {
			engine = new ServerRadioFileEngine(arg);
		} else if(engineType.equals("tcp")) {
			int port = Integer.parseInt(arg);
			engine = new ServerRadioTCPEngine(port);
		} else if(engineType.equals("udp")) {
			int port = Integer.parseInt(arg);
			engine = new ServerRadioUDPEngine(port);
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
