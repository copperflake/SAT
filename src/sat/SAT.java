package sat;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

import sat.plane.Plane;
import sat.radio.RadioID;
import sat.radio.engine.file.FileEngineMessage;
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
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream("socket.in", true), 65535);
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			

			oos.writeObject(new FileEngineMessage(new RadioID("HELLO TOWER!"), new byte[50]));
			bos.flush();
			
			oos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
