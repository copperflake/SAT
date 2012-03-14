package sat;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import sat.crypto.RSAInputStream;
import sat.crypto.RSAKeyPair;
import sat.crypto.RSAOutputStream;
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
			RSAKeyPair kp = new RSAKeyPair(512);
			
			PipedOutputStream pos = new PipedOutputStream();
			PipedInputStream pis = new PipedInputStream(pos);
			
			RSAOutputStream ros = new RSAOutputStream(pos, kp);
			final RSAInputStream ris = new RSAInputStream(pis, kp);
			
			(new Thread() {
				public void run() {
					try {
						ObjectInputStream ois = new ObjectInputStream(ris);
						
						String s;
						while((s = (String) ois.readObject()) != null) {
							System.out.println(s);
						}
						
						ois.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}).start();
			
			ObjectOutputStream oos = new ObjectOutputStream(ros);
			
			oos.writeObject("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vivamus pellentesque porttitor nulla, a elementum mauris fermentum tristique. Sed volutpat dignissim sem, sed tempor justo rhoncus at. Sed vehicula scelerisque metus quis pretium. Morbi feugiat metus nec augue ultricies consequat. Suspendisse vehicula convallis odio, pharetra hendrerit enim euismod nec. Cras est magna, molestie non imperdiet id, bibendum eu dolor. Nullam cursus magna et nisi lobortis pharetra. Vestibulum euismod sem sed felis lacinia tincidunt. Duis nec dui sed justo sagittis volutpat ac sed quam.");
			oos.writeObject("");
			oos.writeObject("Aenean a erat leo, at tincidunt mauris. Sed mi turpis, dignissim varius hendrerit et, dapibus ac ante. Proin consectetur interdum tortor. Sed tempor augue quis quam placerat ullamcorper. Maecenas sollicitudin, turpis ut sagittis ullamcorper, eros metus venenatis ipsum, eget eleifend arcu metus id tortor. Nam augue tortor, cursus et semper a, accumsan et turpis. Aenean id purus velit. In hac habitasse platea dictumst. Nulla consequat libero nec sapien porttitor ornare. Quisque feugiat, turpis a cursus elementum, ligula ligula interdum est, in varius nisi nisi sed lectus. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. Sed vitae nunc dolor, eu blandit nisl. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos.");
			oos.writeObject(null);
			
			oos.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
