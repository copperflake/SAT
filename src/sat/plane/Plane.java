package sat.plane;

import java.io.IOException;

import sat.radio.RadioClient;
import sat.radio.RadioClientDelegate;
import sat.radio.engine.client.RadioClientEngine;

public class Plane implements RadioClientDelegate {
	RadioClient radio;
	
	public void connect(RadioClientEngine engine) throws IOException {
		if(radio == null) radio = new RadioClient(this);
		radio.connect(engine);
	}
	
	public static void main(String[] args) {
		System.out.println("I'm a plane !");
		
		Plane plane = new Plane();
		
		PlaneCLI repl = new PlaneCLI(plane, System.in, System.out);
		Thread replThread = repl.runInNewThread();
	}
}
