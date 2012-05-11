package sat.events;

import java.util.HashSet;
import java.util.Set;

public abstract class EventEmitter<L extends EventListener> {
	protected Set<L> listeners = new HashSet<L>();

	public void addListener(L listener) {
		if(listener != null)
			listeners.add(listener);
	}

	public void removeListener(L listener) {
		listeners.remove(listener);
	}

	protected void emit(Event<L, ?> event) {
		for(L listener : listeners) {
			event.notify(listener);
		}
	}
}
