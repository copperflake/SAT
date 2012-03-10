package sat.plane;

import java.io.InputStream;
import java.io.PrintStream;

import sat.repl.REPL;

public class PlaneREPL extends REPL {
	private Plane plane;
	
	public PlaneREPL(Plane plane, InputStream in, PrintStream out) {
		super(in, out, "Plane> ");
		this.plane = plane;
	}
	
	public void exit() {
		super.exit();
	}
}
