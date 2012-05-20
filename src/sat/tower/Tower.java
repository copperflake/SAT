package sat.tower;

import java.io.IOException;

import sat.events.Event;
import sat.events.EventEmitter;
import sat.events.EventListener;

import sat.radio.RadioEvent;

import sat.radio.RadioID;
import sat.radio.engine.server.RadioServerEngine;
import sat.radio.message.Message;
import sat.radio.server.RadioServer;
import sat.radio.server.RadioServerDelegate;
import sat.utils.cli.Config;
import sat.utils.crypto.RSAException;
import sat.utils.crypto.RSAKeyPair;
import sat.utils.geo.Coordinates;

/**
 * Une tour de contrôle. Cette classe est un Singleton.
 */
public class Tower extends EventEmitter implements EventListener, RadioServerDelegate {
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

		defaults.setProperty("radio.debug", "no");
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
		if(instance == null) {
			initDefaults();
			instance = new Tower();
		}

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
	 * La clé de cette tour de contrôle.
	 */
	private RSAKeyPair keyPair;

	/**
	 * L'identifiant de la tour.
	 */
	private RadioID id;

	/**
	 * Retourne l'objet de configuration de la tour.
	 */
	public Config getConfig() {
		return config;
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
		if(radio == null) {
			radio = new RadioServer(this);
			radio.addListener(this);
		}

		radio.listen(engine);
	}

	// - - - Radio Delegate - - -

	/**
	 * Retourne les coordonées de la tour.
	 */
	public Coordinates getLocation() {
		return new Coordinates(0, 0, 0);
	}

	/**
	 * Retourne l'identifiant utilisé par la radio.
	 */
	public RadioID getRadioID() {
		if(id == null) {
			id = new RadioID("TWR");
		}

		return id;
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

	// - - - Radio Events - - -

	/**
	 * Réception d'un message (cas général)
	 */
	public void on(Message e) {
		System.out.println(e);
	}

	public void on(RadioEvent.PlaneConnected e) {
		System.out.println("Plane connected " + e.getId());
	}

	public void on(RadioEvent.PlaneDisconnected e) {
		System.out.println("Plane disconnected " + e.getId());
	}

	public void on(Event event) {
		// Unmanaged event, pass it to our own listeners
		System.out.println(event);
		emit(event);
	}
}
