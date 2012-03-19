package sat.plane;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import sat.GlobalCLI;
import sat.radio.engine.client.RadioClientEngine;
import sat.radio.engine.client.RadioClientFileEngine;
import sat.radio.engine.client.RadioClientTCPEngine;

public class PlaneCLI extends GlobalCLI {
	private Plane plane;
	
	public PlaneCLI(Plane plane, InputStream in, PrintStream out) {
		super(in, out, "Plane> ");
		this.plane = plane;
	}
	
	public void connect(String engineType, String arg1, String arg2) throws IOException {
		RadioClientEngine engine;
		
		if(engineType.isEmpty()) {
			engineType = "tcp";
		}
		
		if(engineType.equals("file")) {
			out.println("[Warning] `listen file` requires a *nix system and is depreciated!");
			engine = new RadioClientFileEngine(arg1);
		} else if(engineType.equals("tcp")) {
			if(arg1.isEmpty()) arg1 = "localhost"; // Default address
			if(arg2.isEmpty()) arg2 = "6969"; // Default port
			
			InetAddress host = InetAddress.getByName(arg1);
			int port = Integer.parseInt(arg2);
			
			engine = new RadioClientTCPEngine(host, port);
		} else {
			out.println("Error: unknown radio engine type");
			return;
		}
		
		plane.connect(engine);
	}
}
