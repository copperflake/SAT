package sat;

import java.io.FileOutputStream;

import sat.plane.Plane;
import sat.tower.Tower;

public class SAT {
	public static void main(String[] args) {
		if(args.length < 1) {
			usage();
			return;
		}
		
		if(args[0].equals("lab")) {
			lab();
		} else if(args[0].equals("plane")) {
			Plane.main(args);
		} else if(args[0].equals("tower")) {
			Tower.main(args);
		} else {
			System.out.println("Unknown command " + args[0] + "...");
			usage();
		}
	}
	
	public static void usage() {
		System.out.println("Usage: java -jar sat.jar COMMAND <ARGS...>");
		System.out.println("Available commands:");
		System.out.println("    plane  |  Start a plane connected with towerIP");
		System.out.println("    tower  |  Start a tower");
	}
	
	public static void lab() {
		try {
			FileOutputStream fos = new FileOutputStream("socket", true);
			byte[] buffer = {(byte)'H', (byte)'E', (byte)'L', (byte)'L', (byte)'O'};
			fos.write(buffer);
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
