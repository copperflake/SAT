package sat.plane;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;

import sat.EndOfWorldException;
import sat.events.Event;
import sat.events.EventListener;
import sat.radio.RadioID;
import sat.radio.client.RadioClient;
import sat.radio.client.RadioClientDelegate;
import sat.radio.engine.client.RadioClientEngine;
import sat.utils.cli.Config;
import sat.utils.crypto.RSAException;
import sat.utils.crypto.RSAKey;
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
	 * La clé de cet avion.
	 */
	private RSAKeyPair keyPair;

	/**
	 * La position de l'avion.
	 */
	private Coordinates coords;

	/**
	 * L'identifiant de l'avion.
	 */
	private RadioID id;

	private PlaneType type;

	private RadioClient radio;

	private PlaneSimulator simulator;

	private boolean initDone = false;

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

		defaults.setProperty("plane.debug", "no");
		defaults.setProperty("plane.coords", "0,0,0");
		defaults.setProperty("plane.type", "A320");
		defaults.setProperty("plane.prefix", "PLN");

		defaults.setProperty("legacy.towerkey", "tower.key");

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

	public void init() {
		if(initDone) {
			return;
		}

		// Coords
		// TODO: read from config
		coords = new Coordinates(0, 0, 0);

		// Plane Type
		type = PlaneType.getPlaneTypeByName(config.getString("plane.type"));
		if(type == null) { // getPlaneTypeByName return null for unknown types
			throw new EndOfWorldException("Invalid plane type");
		}

		// ID
		id = new RadioID(config.getString("plane.prefix"));

		// Radio
		radio = new RadioClient(this, id);
		radio.addListener(this);

		radio.setCiphered(config.getBoolean("radio.ciphered"));
		radio.setLegacy(config.getBoolean("radio.legacy"));

		initDone = true;
	}

	public void connect(RadioClientEngine engine) throws IOException {
		radio.connect(engine);
	}

	public void start() {
		simulator = new PlaneSimulator();
		simulator.start();
	}

	// - - - Events - - -

	public void on(Event event) {
		System.out.println(event);
	}

	// - - - Plane Simulator - - -

	private class PlaneSimulator extends Thread {

		public PlaneSimulator() {

		}

		public void run() {
			// TODO temp param
			long time = 1000;
			float consumption;
			while(true) {
				try {
					// TODO Diminuer le Kerozène
					// TODO Un avion envoie un message MAYDAY si son niveau de kérosène est inférieur à 20% de sa capacité maximale.
					// TODO Calculer la position de l'avion selon la route
					// TODO Si l'avion est arrivé au bout de sa route, on remove la dernière instruction.
					sleep(time);
				}
				catch(InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		// ROUTING
		//private void moveInStraightLine(double deltaTimeSeconds, RoadInstruction instruction) {
			// TODO
		//}

		//private void moveInCircle(double deltaTimeSeconds, RoadInstruction current) {
			// TODO
		//}

		//private double computeTheta(double px, double py, double cx, double cy) {
		//	return Math.atan2(py - cy, px - cx);
		//}
	}

	// - - - Plane Delegate - - -

	public Coordinates getLocation() {
		return coords;
	}

	public RSAKeyPair getKeyPair() {
		if(keyPair == null) {
			try {
				keyPair = new RSAKeyPair(config.getInt("radio.keylength"));
			}
			catch(RSAException e) {
				// Invalid key length, ignore given length and use default
				keyPair = new RSAKeyPair();
			}
		}

		return keyPair;
	}

	public RSAKeyPair getLegacyTowerKey() {
		try {
			FileInputStream fis = new FileInputStream(config.getString("legacy.towerkey"));
			DataInputStream dis = new DataInputStream(fis);

			@SuppressWarnings("unused")
			int keyLenght = dis.readInt();

			int modulusLength = dis.readInt();
			byte[] modulusBuffer = new byte[modulusLength];
			dis.readFully(modulusBuffer);
			BigInteger modulus = new BigInteger(modulusBuffer);

			int exponentLength = dis.readInt();
			byte[] exponentBuffer = new byte[exponentLength];
			dis.readFully(exponentBuffer);
			BigInteger exponent = new BigInteger(exponentBuffer);

			RSAKey publicKey = new RSAKey(exponent, modulus);
			RSAKeyPair keyPair = new RSAKeyPair(publicKey);

			return keyPair;
		}
		catch(IOException e) {
			System.out.println("Error reading legacy tower key");
			e.printStackTrace();
		}

		// TODO: something better ?
		return null;
	}
}
