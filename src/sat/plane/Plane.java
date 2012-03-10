package sat.plane;

public class Plane {
	public static void main(String[] args) {
		System.out.println("I'm a plane !");
		
		PlaneREPL repl = new PlaneREPL(System.in, System.out);
		Thread replThread = repl.runInNewThread();
	}
}
