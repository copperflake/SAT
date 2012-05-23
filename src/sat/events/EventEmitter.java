package sat.events;

import java.util.HashSet;
import java.util.Set;

/**
 * Un émetteur d'événement. Cette classe fourni les méthodes de bases pour
 * associer des EventListeners à un émetteur et gérer le processus d'émission.
 * 
 * Cette version basique d'EventEmitter utilise une émission synchrone. Le
 * thread émetteur est bloqué tant que la distribution dun événement n'est pas
 * terminée.
 */
public abstract class EventEmitter implements EventEmitterInterface {
	/**
	 * La liste des EventListeners associés à cet émetteur.
	 */
	protected Set<EventListener> listeners = new HashSet<EventListener>();

	/**
	 * L'objet qui sera associé comme émetteur pour les événements émis par cet
	 * émetteur. Ceci permet, avec la classe EventEmitterInterface, d'émuler le
	 * comportement d'un EventListener complet dans une classe qui ne pourrait
	 * pas en hériter.
	 */
	protected EventEmitterInterface emitter;

	/**
	 * Crée un nouveau émetteur d'événement.
	 */
	public EventEmitter() {
		this(null);
	}

	/**
	 * Crée un nouveau émetteur d'événement émettant des événements pour le
	 * compte d'un autre éméteur ou pseudo-émetteur.
	 * 
	 * @param emitter
	 *            L'émetteur à utiliser comme origine pour les événements émis.
	 *            Par défaut soi-même si null.
	 */
	public EventEmitter(EventEmitterInterface emitter) {
		this.emitter = emitter != null ? emitter : this;
	}

	/**
	 * Ajoute un gestionnaire aux événements de cet émétteur.
	 */
	public void addListener(EventListener listener) {
		if(listener != null) {
			listeners.add(listener);
		}
	}

	/**
	 * Retire un gestionnaire de cet émetteur.
	 */
	public void removeListener(EventListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Emet un événement.
	 */
	public void emit(Event event) {
		for(EventListener listener : listeners) {
			try {
				event.trigger(listener, emitter);
			}
			catch(Exception e) {
				// Ignore exceptions when emitting events
			}
		}
	}
}
