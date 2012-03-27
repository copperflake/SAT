package sat.plane;

import java.io.IOException;

import sat.radio.RadioClient;
import sat.radio.RadioClientDelegate;
import sat.radio.engine.client.RadioClientEngine;
import sat.utils.cli.Config;
import sat.utils.geo.Coordinates;

public class Plane implements RadioClientDelegate {
	/**
	 * La configuration par défaut d'un avion. Sert de modèle à la contruction
	 * de la configuration spécifique aux instances d'avions.
	 */
	private static Config defaults;

	/**
	 * La configuration spécifique à une instance d'un avion.
	 */
	private Config config;

	private RadioClient radio;

	public Plane() {
		if(defaults == null)
			initDefaults();

		config = new Config(defaults);
	}

	/**
	 * Initialise la configuration par défaut avec les valeurs appropriées.
	 */
	private static void initDefaults() {
		defaults = new Config();

		defaults.setProperty("legacy", "no");
	}

	/**
	 * Retourne l'objet de configuration de l'avion.
	 */
	public Config getConfig() {
		return config;
	}

	public void connect(RadioClientEngine engine) throws IOException {
		if(radio == null)
			radio = new RadioClient(this);
		radio.connect(engine);
	}

	public static void main(String[] args) {
		System.out.println("I'm a plane !");

		Plane plane = new Plane();

		PlaneCLI repl = new PlaneCLI(plane, System.in, System.out);
		Thread replThread = repl.runInNewThread();
	}

	// - - - Delegate - - -

	public Coordinates getLocation() {
		return new Coordinates(0, 0, 0);
	}
}
