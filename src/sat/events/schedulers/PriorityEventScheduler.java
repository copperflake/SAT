package sat.events.schedulers;

import java.util.PriorityQueue;

import sat.events.Event;

public class PriorityEventScheduler extends QueueEventScheduler {
	public PriorityEventScheduler() {
		super(new PriorityQueue<Event>());
	}
}
