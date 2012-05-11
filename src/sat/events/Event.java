package sat.events;

public abstract class Event<L extends EventListener, E extends EventEmitter<L>> {
	private E emitter;

	public Event(E emitter) {
		this.emitter = emitter;
	}

	public E emitter() {
		return emitter;
	}

	public abstract void notify(L listener);
}
