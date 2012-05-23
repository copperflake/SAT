package sat.events;

import sat.events.schedulers.EventScheduler;
import sat.events.schedulers.QueueEventScheduler;

/**
 * Un émetteur d'événement asynchrone. Contrairement à un simple EventEmitter,
 * cette classe utilise un thread d'émission. Par conséquent, l'appel à la
 * fonction emit est non-bloquante.
 * 
 * Note: si l'émission est non-bloquante, la distribution est toujours
 * synchrone. Les événements sont placés dans une file d'attente et distribués
 * séquentiellement. Chaque AsyncEventEmitter est donc associé au maximum à un
 * thread de distribution.
 * 
 * Cette méthode a pour but de garantir le temps de distribtution pour
 * l'appelant (temps nul) mais ne garanti pas la latence de réception pour les
 * gestionnaires.
 */
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
