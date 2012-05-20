package sat.plane;

import java.io.IOException;

import sat.events.EventListener;
import sat.radio.RadioID;
import sat.radio.client.RadioClient;
import sat.radio.client.RadioClientDelegate;
import sat.radio.engine.client.RadioClientEngine;
import sat.utils.cli.Config;
import sat.utils.crypto.RSAKeyPair;
import sat.utils.geo.Coordinates;

public class Plane implements EventListener, RadioClientDelegate {
	/**
	 * La configuration par défaut d'un avion. Sert de modèle à la contruction
	 * de la configuration spécifique aux instances d'avions.
	 */
	private static Config defaults;

	/**
	 * La configuration spécifique à une instance d'un avion.
	 */
	private Config config;
	
	/**
	 * La position de l'avion.
	 */
	private Coordinates coords;

	/**
	 * L'identifiant de l'avion.
	 */
	private RadioID id;

	private RadioClient radio;
	
	private PlaneType type;
	
	private PlaneSimulator simulator;

	public Plane(PlaneType type) {
		coords = new Coordinates(0, 0, 0);
		
		if(defaults == null)
			initDefaults();

		config = new Config(defaults);
		
		this.type = type;
	}

	/**
	 * Initialise la configuration par défaut avec les valeurs appropriées.
	 */
	private static void initDefaults() {
		defaults = new Config();

		defaults.setProperty("radio.debug", "no");
		defaults.setProperty("radio.ciphered", "yes");
		defaults.setProperty("radio.legacy", "no");
		defaults.setProperty("radio.keylength", "1024");
	}

	/**
	 * Retourne l'objet de configuration de l'avion.
	 */
	public Config getConfig() {
		return config;
	}

	public void connect(RadioClientEngine engine) throws IOException {
		if(radio == null) {
			radio = new RadioClient(this);
		}
		
		radio.connect(engine);
	}
	
	public void start() {
		simulator = new PlaneSimulator();
		simulator.start();
	}
	
	// - - - Plane Simulator - - -

	private class PlaneSimulator extends Thread {
		public void run() {
			// TODO
		}
	}
	
	// - - - Plane Delegate - - -

	public Coordinates getLocation() {
		return coords;
	}
	
	public void setLocation(Coordinates coords) {
		this.coords = coords;
	}

	/**
	 * Retourne l'identifiant utilisé par la radio.
	 */
	public RadioID getRadioID() {
		if(id == null) {
			id = new RadioID("PLN");
		}

		return id;
	}

	public RSAKeyPair getKeyPair() {
		return null;
	}
}
