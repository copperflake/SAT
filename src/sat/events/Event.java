package sat.events;

public interface Event<T extends EventListener> {
	public abstract void notify(T listener);
}
