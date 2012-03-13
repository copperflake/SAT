package sat;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import sat.plane.Plane;
import sat.radio.RadioID;
import sat.radio.engine.file.FileEngineMessage;
import sat.radio.message.MessageKeepalive;
import sat.radio.message.MessageMayDay;
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
			Socket sock = new Socket("localhost", 4242);
			
			ObjectOutputStream oos = new ObjectOutputStream(sock.getOutputStream());

			oos.writeObject(new MessageKeepalive());
			oos.writeObject(new MessageKeepalive());
			oos.writeObject(new MessageKeepalive());
			oos.writeObject(new MessageKeepalive());
			oos.writeObject(new MessageKeepalive());
			oos.writeObject(new MessageKeepalive());
			oos.writeObject(new MessageKeepalive());
			oos.writeObject(new MessageKeepalive());
			oos.writeObject(new MessageKeepalive());
			oos.writeObject(new MessageMayDay("ALL MY BASE ARE BELONG TO THEM"));
			oos.writeObject(null);
	
			oos.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
