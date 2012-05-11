package sat.events;

import java.util.HashSet;
import java.util.Set;

public abstract class EventEmitter<T extends EventListener> {
	protected Set<T> listeners = new HashSet<T>();

	public void addListener(T listener) {
		if(listener != null)
			listeners.add(listener);
	}

	public void removeListener(T listener) {
		listeners.remove(listener);
	}

	protected void emit(Event<T> event) {
		for(T listener : listeners) {
			event.notify(listener);
		}
	}
}
