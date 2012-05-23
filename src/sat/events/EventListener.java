package sat.events;

/**
 * Une interface de tag définissant un gestionnaire d'événement. Puisque les
 * gestionnaire sont libre d'attraper ou non chaque type d'événement, aucune
 * obligation n'est fait sur les méthodes à implémenter.
 * 
 * Cependant, il est attendu d'une classe implémentant EventListener qu'elle
 * implémente au moins une fonction on(<? extends Event>).
 * 
 * De plus, un gestionnaire d'événement doit s'executer rapidement pour ne pas
 * bloquer le processus de distribution d'événement. Si un traitement lourd doit
 * être effectué par un gestionnaire, celui-ci doit en différer l'execution.
 */
public interface EventListener {
	//public abstract void on(Event event);
}
