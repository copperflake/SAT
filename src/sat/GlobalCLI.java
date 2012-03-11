package sat;

import java.io.InputStream;
import java.io.PrintStream;

import sat.cli.CLI;

public class GlobalCLI extends CLI {
	public GlobalCLI(InputStream in, PrintStream out) {
		super(in, out, "SAT> ");
	}
	
	public GlobalCLI(InputStream in, PrintStream out, String prompt) {
		super(in, out, prompt);
	}
	
	public void exit() {
		super.exit();
	}
}
