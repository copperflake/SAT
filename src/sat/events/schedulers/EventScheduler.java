package sat.events.schedulers;

import sat.events.Event;

public interface EventScheduler {
	public abstract void addEvent(Event event);
	public abstract Event nextEvent();
}
