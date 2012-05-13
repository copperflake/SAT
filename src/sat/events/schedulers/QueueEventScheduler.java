package sat.events.schedulers;

import java.util.ArrayDeque;
import java.util.Queue;

import sat.events.Event;

public class QueueEventScheduler implements EventScheduler {
	private Queue<Event> queue;
	
	public QueueEventScheduler() {
		this(new ArrayDeque<Event>());
	}
	
	public QueueEventScheduler(Queue<Event> queue) {
		this.queue = queue;
	}

	public synchronized Event nextEvent() {
		return queue.poll();
	}

	public synchronized void addEvent(Event event) {
		queue.offer(event);
	}
}
