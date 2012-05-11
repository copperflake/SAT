package sat.radio.client;

import java.io.IOException;

import sat.radio.Radio;
import sat.radio.engine.client.RadioClientEngine;
import sat.radio.engine.client.RadioClientEngineDelegate;

/**
 * Un client radio.
 * <p>
 * Un client radio est capable de se connecter à un seveur radio pour
 * communiquer.
 * <p>
 * À la différence d'un serveur radio, le client radio ne peut pas accepter de
 * connexion entrante et n'est pas capable de se connecter à plus d'un hôte.
 */
public class RadioClient extends Radio implements RadioClientEngineDelegate {
	/**
	 * Le délégué de la radio. Le délégué reçoit les évenements émis par la
	 * radio et est chargé de leur gestion.
	 */
	RadioClientDelegate delegate;

	/**
	 * Le moteur de communication de la radio.s
	 */
	RadioClientEngine engine;

	/**
	 * Crée un client radio utilisant le délégué <code>delegate</code> pour
	 * gérer les évenements.
	 * 
	 * @param delegate
	 *            Le délégué chargé de la gestion des événements.
	 */
	public RadioClient(RadioClientDelegate delegate, int keylength, String label) {
		super(label, keylength);

		this.delegate = delegate;
	}

	/**
	 * Connecte le client radio à un serveur radio en utilisant le moteur de
	 * communcation <code>engine</code> spécifié. Le moteur passé en paramètre
	 * est automatiquement initialisé.
	 * 
	 * @param engine
	 *            Le moteur de communication utilisé pour la connexion
	 * 
	 * @throws IOException
	 *             L'initialisation du moteur de communication peut provoquer
	 *             une exception qui est passée au code appelant. De plus, si
	 *             cette radio est déjà associée à un moteur de communication,
	 *             une exception sera levée.
	 */
	public void connect(RadioClientEngine engine) throws IOException {
		// Un client radio ne peut avoir qu'un seul moteur actif.
		if(this.engine != null)
			throw new IOException("This radio already have a registered engine.");

		try {
			this.engine = engine;
			this.engine.init(this);
		}
		catch(IOException e) {
			// Reset the engine if initialization was not successful.
			this.engine = null;

			// Rethrow exception
			throw e;
		}
	}
}
