package sat.events.schedulers;

import java.util.ArrayDeque;
import java.util.Queue;

import sat.events.Event;

/**
 * Ordonnanceur d'événements basé sur un simple queue. Les événements sont
 * distribués dans l'ordre où ils ont été émis.
 */
public class QueueEventScheduler implements EventScheduler {
	/**
	 * La queue d'événement à utiliser pour stocker les événements à délivrer.
	 */
	private Queue<Event> queue;

	/**
	 * Crée un nouvel ordonnanceur avec une queue simple de type ArrayDeque.
	 */
	public QueueEventScheduler() {
		this(new ArrayDeque<Event>());
	}

	/**
	 * Crée un nouvel ordonnanceur avec une queue spécifique.
	 */
	public QueueEventScheduler(Queue<Event> queue) {
		this.queue = queue;
	}

	/**
	 * Retourne le prochain événement dans la queue.
	 */
	public synchronized Event nextEvent() {
		return queue.poll();
	}

	/**
	 * Ajoute un événement à la fin de la queue.
	 */
	public synchronized void addEvent(Event event) {
		queue.offer(event);
	}
}
