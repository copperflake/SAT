package sat.events.schedulers;

import java.util.PriorityQueue;

import sat.events.Event;
import sat.events.PriorityEvent;

/**
 * Ordonnanceur d'événements basé sur une queue de priorité. Les événements de
 * cet ordonnanceur sont délivrés en fonction de leur priorité.
 * 
 * Cet ordonnanceur est construit autour de la classe générique PriorityEvent.
 * Il intègre deux queues séparées: la première est une simple queue FIFO, la
 * seconde est une queue de priorité. Ceci permet de gérer à la fois des
 * événements avec priorité (de type PriorityEvent) et des événements standards
 * de type Event. Dans le cas où ces deux types d'événement sont ajoutés à cet
 * ordonnanceur, les événements sans priorité sont délivrés avant les événements
 * avec priorité.
 * 
 * Cet ordonnanceur ne fait pas la différence entre les événements de type
 * PriorityEvent. Néanmoins, deux sous-classe non-compatibles de PriorityEvent
 * ne peuvent être ordonnée correctement. Il est donc important de ne pas
 * mélanger les types d'événements prioritaires. Les événements sans priorité
 * peuvent être mélangés sans problèmes.
 */
public class PriorityEventScheduler extends QueueEventScheduler {
	PriorityQueue<PriorityEvent<?>> priorityQueue;

	/**
	 * Crée un nouvel ordonnanceur avec gestion de priorité.
	 */
	public PriorityEventScheduler() {
		priorityQueue = new PriorityQueue<PriorityEvent<?>>();
	}

	/**
	 * Ajoute un événement.
	 */
	public synchronized void addEvent(Event event) {
		if(event instanceof PriorityEvent<?>) {
			priorityQueue.offer((PriorityEvent<?>) event);
		}
		else {
			super.addEvent(event);
		}
	}

	/**
	 * Retourne le prochain événement.
	 */
	public synchronized Event nextEvent() {
		Event event;
		if((event = super.nextEvent()) != null) {
			return event;
		}

		return priorityQueue.poll();
	}
}
