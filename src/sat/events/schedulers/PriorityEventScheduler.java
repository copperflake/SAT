package sat.events.schedulers;

import java.util.PriorityQueue;

import sat.events.Event;
import sat.events.PriorityEvent;

public class PriorityEventScheduler extends QueueEventScheduler {
	PriorityQueue<PriorityEvent<?>> priorityQueue;

	public PriorityEventScheduler() {
		priorityQueue = new PriorityQueue<PriorityEvent<?>>();
	}

	public synchronized void addEvent(Event event) {
		if(event instanceof PriorityEvent<?>) {
			priorityQueue.offer((PriorityEvent<?>) event);
		}
		else {
			super.addEvent(event);
		}
	}

	public synchronized Event nextEvent() {
		Event event;
		if((event = super.nextEvent()) != null) {
			return event;
		}

		return priorityQueue.poll();
	}
}
