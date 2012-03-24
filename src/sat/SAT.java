package sat;

import sat.plane.Plane;
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
			Lab.lab(args);
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
}
