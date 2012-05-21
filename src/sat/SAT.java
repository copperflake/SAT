package sat;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;

import sat.events.Event;
import sat.events.EventListener;
import sat.plane.Plane;
import sat.plane.PlaneCLI;
import sat.tower.TowerCLI;
import sat.tower.agent.RemoteTowerAgent;

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
		}
		else if(args[0].equals("plane")) {
			initPlane(args);
		}
		else if(args[0].equals("tower")) {
			initTower(args);
		}
		else if(args[0].equals("remote")) {
			try {
				RemoteTowerAgent remote = new RemoteTowerAgent();
				remote.addListener(new EventListener() {
					@SuppressWarnings("unused")
					public void on(Event event) {
						System.out.println("Got event: " + event);
					}
				});
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
		else {
			// Commande inconnue
			System.out.println("Unknown command " + args[0] + "...");
			usage();
		}
	}

	/**
	 * Main method, appelée quand SAT est executé avec la commande
	 * <code>./sat tower</code>.
	 * 
	 * @param args
	 *            Les paramètres de la ligne de commande. Ces paramètres ne sont
	 *            pas utilisés.
	 */
	public static void initTower(String[] args) {
		System.out.println("I'm a tower !");

		// Tower is lazily instantiated when starting the CLI (the CLI calls getInstance())
		//Tower tower = Tower.getInstance();

		TowerCLI cli = new TowerCLI(System.in, System.out);

		if(args.length > 1) {
			// Run commands provided
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(args[1])));

				String line;
				while((line = reader.readLine()) != null) {
					cli.eval(line);
				}
			}
			catch(IOException e) {
				e.printStackTrace();
			}
		}

		cli.runInNewThread();
	}

	public static void initPlane(String[] args) {
		System.out.println("I'm a plane !");

		Plane plane = new Plane();

		PlaneCLI cli = new PlaneCLI(plane, System.in, System.out);

		if(args.length > 1) {
			// Run commands provided
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(args[1])));

				String line;
				while((line = reader.readLine()) != null) {
					cli.eval(line);
				}
			}
			catch(IOException e) {
				e.printStackTrace();
			}
		}

		cli.runInNewThread();
	}

	// TODO
	public static void initPlaneLegacy(String[] args) {
		System.out.println("I'm a legacy plane !");

		Plane plane = new Plane();

		PlaneCLI cli = new PlaneCLI(plane, System.in, System.out);
		cli.runInNewThread();
	}

	/**
	 * Affiche les instructions d'utilisation.
	 */
	public static void usage() {
		System.out.println("Usage: ./sat COMMAND");
		System.out.println("Available commands:");
		System.out.println("    plane  |  Start a plane");
		System.out.println("    tower  |  Start a tower");
	}
}
