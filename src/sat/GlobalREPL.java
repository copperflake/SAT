package sat;

import java.io.InputStream;
import java.io.PrintStream;

import sat.repl.REPL;

public class GlobalREPL extends REPL {
	public GlobalREPL(InputStream in, PrintStream out) {
		super(in, out, "SAT> ");
	}
	
	public GlobalREPL(InputStream in, PrintStream out, String prompt) {
		super(in, out, prompt);
	}
	
	public void exit() {
		super.exit();
	}
}
