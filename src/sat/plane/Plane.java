package sat.plane;

public class Plane {
	public static void main(String[] args) {
		System.out.println("I'm a plane !");
		
		Plane plane = new Plane();
		
		PlaneCLI repl = new PlaneCLI(plane, System.in, System.out);
		Thread replThread = repl.runInNewThread();
	}
}
