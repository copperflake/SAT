package sat.events.schedulers;

import sat.events.Event;

/**
 * L'interface exposée par un ordonnanceur d'événements.
 * 
 * Un ordonnanceur d'événements est un objet chargé de maintenir une liste
 * d'événements à distribuer et de décider dans quel ordre les distribuer.
 */
public interface EventScheduler {
	/**
	 * Ajoute un nouvel événement à cet ordonnanceur.
	 */
	public abstract void addEvent(Event event);

	/**
	 * Demande le prochain événement selon l'ordre imposé par cet ordonnanceur.
	 */
	public abstract Event nextEvent();
}
