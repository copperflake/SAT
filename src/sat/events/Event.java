package sat.events;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Un événement. Cette classe représente un événement pouvant être émit par un
 * EventEmitter et reçu par un EventListener. Il ne possède aucune propriété et
 * doit être étendu pour lui associer une signification.
 */
@SuppressWarnings("serial")
public class Event implements Cloneable, Serializable {
	/**
	 * L'émetteur qui a émit cet événement. Cet attribut n'est pas sérialisé
	 * avec l'événement, dans un tel cas, l'émetteur original est perdu.
	 */
	transient private EventEmitterInterface emitter;

	/**
	 * Retourne l'émetteur de cet événement.
	 */
	public EventEmitterInterface getEmitter() {
		return emitter;
	}

	/**
	 * Notifie un EventListener précis que cet événement est survenu.
	 * 
	 * @param listener
	 *            L'EventListener à notifier.
	 * 
	 * @throws UnhandledEventException
	 *             Si l'événement n'est pas géré par le listener spécifié.
	 * @throws InvocationTargetException
	 *             Si l'execution du listener a provoqué une exception.
	 */
	public final void trigger(EventListener listener) throws UnhandledEventException, InvocationTargetException {
		trigger(listener, null);
	}

	/**
	 * Notifie un EventListener précis que cet événement est survenu comme s'il
	 * provenait d'un émetteur donné.
	 * 
	 * @param listener
	 *            L'EventListener à notifier.
	 * @param emitter
	 *            L'émetteur de cet événement.
	 * 
	 * @throws UnhandledEventException
	 *             Si l'événement n'est pas géré par le listener spécifié.
	 * @throws InvocationTargetException
	 *             Si l'execution du listener a provoqué une exception.
	 */
	public final void trigger(EventListener listener, EventEmitterInterface emitter) throws UnhandledEventException, InvocationTargetException {
		// Event itself is not modified
		Event event = (Event) this.clone();

		// Set the emitter
		event.emitter = emitter;

		// --------------------------------------
		// In memoriam of Generics-powered events
		//     "Because *this*, doesnt work"
		// --------------------------------------

		// The class of the event
		Class<?> eventClass = event.getClass();

		while(eventClass != null) {
			try {
				// Try to get a handler for the exact class of this event
				Method on = listener.getClass().getMethod("on", eventClass);

				// Ensure accessibility
				on.setAccessible(true); // Inner-class are otherwise unavailable

				// Invoke!
				on.invoke(listener, event);

				// Done
				return;
			}
			catch(InvocationTargetException e) {
				// Invocation throwed an exception, rethrow it
				throw e;
			}
			catch(Exception e) {
				// Exception when getting the handler, handler is probably undefined

				if(eventClass == Event.class) {
					// Event is the super-class of all events
					break;
				}

				// Try 
				eventClass = eventClass.getSuperclass();
			}
		}

		// Event has not be catched
		throw new UnhandledEventException();
	}

	public Object clone() {
		try {
			return super.clone();
		}
		catch(CloneNotSupportedException e) {
			// Should not happen
			return this;
		}
	}
}
