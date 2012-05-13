package sat.events;

public class Event implements Cloneable {
	private EventEmitterInterface emitter;

	public EventEmitterInterface emitter() {
		return emitter;
	}

	public final void notify(EventListener listener) throws UnhandledEventException {
		notify(listener, null);
	}

	public final void notify(EventListener listener, EventEmitterInterface emitter) throws UnhandledEventException {
		Event event = (Event) this.clone();
		event.emitter = emitter;

		// In memoriam of Generics-powered events

		Class<?> eventClass = event.getClass();

		while(eventClass != null) {
			try {
				listener.getClass().getMethod("on", new Class<?>[] { eventClass }).invoke(listener, event);
				return;
			}
			catch(ReflectiveOperationException e) {
				if(eventClass == Event.class) {
					break;
				}

				eventClass = eventClass.getSuperclass();
			}
		}

		throw new UnhandledEventException();
	}

	public Object clone() {
		try {
			return super.clone();
		}
		catch(CloneNotSupportedException e) {
			return this;
		}
	}
}
