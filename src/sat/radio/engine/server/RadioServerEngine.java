package sat.radio.engine.server;

import java.io.IOException;

import sat.radio.engine.RadioEngine;

/**
 * Un moteur de radio pour un serveur radio.
 */
public abstract class RadioServerEngine extends RadioEngine {
	/**
	 * Le délégué responsable des événements lors de l'execution du moteur.
	 */
	protected RadioServerEngineDelegate delegate;

	/**
	 * Initialisation différée du moteur.
	 */
	public abstract void init(RadioServerEngineDelegate delegate) throws IOException;
}
