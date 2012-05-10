package sat.tower;

import java.io.IOException;
import java.util.Observable;

import sat.gui3D.Radar;
import sat.radio.RadioID;
import sat.radio.engine.server.RadioServerEngine;
import sat.radio.message.Message;
import sat.radio.server.RadioServer;
import sat.radio.server.RadioServerDelegate;
import sat.utils.cli.Config;
import sat.utils.crypto.RSAKeyPair;
import sat.utils.geo.Coordinates;

/**
 * Une tour de contrôle. Cette classe est un Singleton.
 */
public class Tower extends Observable {
	// - - - Singleton Tools - - -

	/**
	 * L'instance unique de la classe Tower.
	 */
	private static Tower instance;

	/**
	 * La configuration par défaut d'une tour. Sert de modèle à la contruction
	 * de la configuration spécifique aux instances d'une tour.
	 */
	private static Config defaults;

	/**
	 * Initialise la configuration par défaut avec les valeurs appropriées.
	 */
	private static void initDefaults() {
		defaults = new Config();

		defaults.setProperty("radio.ciphered", "yes");
		defaults.setProperty("radio.legacy", "no");
		defaults.setProperty("radio.keylength", "1024");
	}

	/**
	 * Constructeur privé. Impossible de créer une instance de cette classe
	 * directement. Utiliser <code>Tower.getInstance()</code> pour obtenir
	 * l'instance-unique de la tour.
	 */
	private Tower() {
		config = new Config(defaults);
	}

	/**
	 * Retourne l'unique instance de la classe Tower. Si cette instance n'existe
	 * pas encore, elle est automatiquement créée.
	 * 
	 * @return L'instance unique de la classe Tower.
	 */
	public static Tower getInstance() {
		if(defaults == null)
			initDefaults();

		if(instance == null)
			instance = new Tower();

		return instance;
	}

	// - - - Class methods - - -

	/**
	 * Le serveur-radio de la tour. Le serveur radio est chargé de toute la
	 * gestion technique de la communication avec le monde extérieur.
	 */
	private RadioServer radio = null;

	/**
	 * La configuration spécifique à une instance de la tour (même si en
	 * pratique, la tour est un singleton).
	 */
	private Config config;

	/**
	 * Retourne l'objet de configuration de la tour.
	 */
	public Config getConfig() {
		return config;
	}

	/**
	 * Retourne les coordonées de la tour.
	 */
	public Coordinates getLocation() {
		return new Coordinates(0, 0, 0);
	}

	/**
	 * Ajoute un moteur de radio à la radio de la tour et l'initialise.
	 * 
	 * @param engine
	 *            Le moteur de radio à ajouter.
	 * 
	 * @throws IOException
	 *             L'initilisation du moteur peut provoquer une
	 *             <code>IOException</code> qui est passée au code appelant.
	 */
	public void listen(RadioServerEngine engine) throws IOException {
		lazyRadioInit();
		radio.listen(engine);
	}

	public RSAKeyPair getKeyPair() {
		lazyRadioInit();
		return radio.getKeyPair();
	}

	private void lazyRadioInit() {
		if(radio == null)
			radio = new RadioServer(new Delegate(), "TWR");
	}

	// - - - Main method - - -
	
	public void startGui() {
		Radar.launch();
	}

	// - - - Delegate - - -

	private class Delegate implements RadioServerDelegate {
		public Config getConfig() {
			return Tower.this.getConfig();
		}

		public Coordinates getLocation() {
			return Tower.this.getLocation();
		}

		public void onPlaneConnected(RadioID plane) {
			System.out.println("Plane " + plane + " connected");
		}

		public void onPlaneDisconnected(RadioID plane) {
			System.out.println("Plane " + plane + " disconnected");
		}

		public void onMessage(RadioID plane, Message message) {
			switch(message.getType()) {
				case KEEPALIVE:
					System.out.println("Tower got keepalive from " + plane);
					break;

				default:
					System.out.println("Tower cannot handle message " + message.getType());
					radio.kick(plane);
			}
		}
	}
}
