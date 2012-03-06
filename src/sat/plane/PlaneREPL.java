package sat.plane;

import java.io.InputStream;
import java.io.PrintStream;

import sat.repl.REPL;

public class PlaneREPL extends REPL {
	public PlaneREPL(InputStream in, PrintStream out) {
		super(in, out, "Plane> ");
	}
	
	public void boum() {
		System.exit(0);
	}
}
