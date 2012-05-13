package sat.events;

import sat.events.schedulers.EventScheduler;
import sat.events.schedulers.QueueEventScheduler;

public class AsyncEventEmitter extends EventEmitter {
	private Event activeEvent;
	private EventScheduler scheduler;

	public AsyncEventEmitter() {
		this(null, null);
	}

	public AsyncEventEmitter(EventEmitterInterface emitter) {
		this(emitter, null);
	}

	public AsyncEventEmitter(EventScheduler scheduler) {
		this(null, scheduler);
	}

	public AsyncEventEmitter(EventEmitterInterface emitter, EventScheduler scheduler) {
		super(emitter);

		if(scheduler == null) {
			scheduler = new QueueEventScheduler();
		}

		this.scheduler = scheduler;
	}

	public synchronized void emit(Event event) {
		scheduler.addEvent(event);

		if(activeEvent == null) {
			nextEvent();

			new Thread() {
				public void run() {
					while(activeEvent != null) {
						try {
							AsyncEventEmitter.super.emit(activeEvent);
						}
						finally {
							nextEvent();
						}
					}
				}
			}.start();
		}
	}

	private synchronized void nextEvent() {
		activeEvent = scheduler.nextEvent();
	}
}
