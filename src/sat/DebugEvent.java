package sat;

import sat.events.Event;

/**
 * Evenement de débuggage. Il est émis par différents composants de
 * l'application pour signaler des événements sans importance mais pouvant être
 * utiles pour le débuggage.
 */
@SuppressWarnings("serial")
public class DebugEvent extends Event {
	/**
	 * Le message associé à cet événement de débuggage.
	 */
	public String message;

	/**
	 * Crée un événement de débuggage avec un message donné.
	 */
	public DebugEvent(String msg) {
		message = msg;
	}

	/**
	 * Récupère le message associé à cet événement.
	 */
	public String getMessage() {
		return message;
	}
}
