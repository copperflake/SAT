package sat.tower;

import java.io.IOException;

import sat.radio.RadioServer;
import sat.radio.engine.server.RadioServerEngine;
import sat.radio.RadioServerDelegate;

/**
 * Une tour de contrôle. Cette classe est un Singleton.
 */
public class Tower implements RadioServerDelegate {
	// - - - Singleton Tools - - -

	/**
	 * L'instance unique de la classe Tower.
	 */
	private static Tower instance = null;

	/**
	 * Constructeur privé. Impossible de créer une instance de cette classe
	 * directement. Utiliser <code>Tower.getInstance()</code> pour obtenir
	 * l'instance-unique de la tour.
	 */
	private Tower() {
	}

	/**
	 * Retourne l'unique instance de la classe Tower. Si cette instance n'existe
	 * pas encore, elle est automatiquement créée.
	 * 
	 * @return L'instance unique de la classe Tower.
	 */
	public static Tower getInstance() {
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
		if(radio == null)
			radio = new RadioServer(this);
		radio.listen(engine);
	}

	// - - - Main method - - -

	/**
	 * Main method, appelée quand SAT est executé avec la commande
	 * <code>./sat tower</code>.
	 * 
	 * @param args
	 *            Les paramètres de la ligne de commande. Ces paramètres ne sont
	 *            pas utilisés.
	 */
	public static void main(String[] args) {
		System.out.println("I'm a tower !");

		Tower tower = getInstance();

		TowerCLI repl = new TowerCLI(tower, System.in, System.out);
		Thread replThread = repl.runInNewThread();
	}
}
