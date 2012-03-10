package sat.plane;

public class Plane {
	public static void main(String[] args) {
		System.out.println("I'm a plane !");
		
		Plane plane = new Plane();
		
		PlaneREPL repl = new PlaneREPL(plane, System.in, System.out);
		Thread replThread = repl.runInNewThread();
	}
}
