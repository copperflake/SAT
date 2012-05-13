package sat.events;

public interface EventEmitterInterface {
	public abstract void addListener(EventListener listener);
	public abstract void removeListener(EventListener listener);
}
