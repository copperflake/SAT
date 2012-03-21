package sat;

import java.io.ObjectOutputStream;
import java.net.Socket;

import sat.plane.Plane;
import sat.radio.RadioID;
import sat.radio.message.MessageKeepalive;
import sat.tower.Tower;

/**
 * Classe principale du programme.
 */
public final class SAT {
	/**
	 * Cette classe est une classe utilitaire et ne peut pas être instanciée.
	 */
	private SAT() {
	}

	/**
	 * Dispatcheur, lis le premier paramètre de la ligne de commande et invoque
	 * la méthode main() de la classe appropriée.
	 * 
	 * @param args
	 *            Les arguments de la ligne de commande
	 */
	public static void main(String[] args) {
		// Aucun argument
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
			// Commande inconnue
			System.out.println("Unknown command " + args[0] + "...");
			usage();
		}
	}

	/**
	 * Affiche les instructions d'utilisation.
	 */
	public static void usage() {
		System.out.println("Usage: java -jar sat.jar COMMAND <ARGS...>");
		System.out.println("Available commands:");
		System.out.println("    plane  |  Start a plane connected with towerIP");
		System.out.println("    tower  |  Start a tower");
	}

	/**
	 * Méthode utilitaire. Cette méthode est utilisée pour executer facilement
	 * un morceau de code lors du développement. Elle n'a aucune utilité dans
	 * l'application finale.
	 */
	public static void lab() {
		try {
			Socket sock = new Socket("localhost", 6969);
			ObjectOutputStream ois = new ObjectOutputStream(sock.getOutputStream());

			ois.writeObject(new MessageKeepalive(new RadioID(), 0, 0));
			ois.writeObject(new MessageKeepalive(new RadioID(), 0, 0));

			ois.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
