package sat.events;

public abstract class Event<L extends EventListener, E extends EventEmitter<L>> {
	public EventEmitter<L> emitter;

	public Event(E emitter) {
		this.emitter = emitter;
	}

	public abstract void notify(L listener);
}
