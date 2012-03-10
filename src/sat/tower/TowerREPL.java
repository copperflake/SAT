package sat.tower;

import java.io.InputStream;
import java.io.PrintStream;

import sat.repl.REPL;

public class TowerREPL extends REPL {
	private Tower tower;
	
	public TowerREPL(Tower tower, InputStream in, PrintStream out) {
		super(in, out, "Tower> ");
		this.tower = tower;
	}
	
	public void exit() {
		super.exit();
	}
}
