package sat.events;

/**
 * Une interface de pseudo-émetteur d'événement. Un objet implémentant cette
 * classe fourni les méthodes caractéristiques d'un EventListener. Il est
 * conseillé d'utiliser cette interface chaque fois qu'un EventListener est
 * attendu pour permettre la réalisation de pseudo-émetteur.
 * 
 * Cette construction est nécessaire si une classe souahite émettre des
 * événements mais que'elle ne peut pas hériter simplement d'EventEmitter car
 * elle hérite déjà d'une autre classe. Dans ce cas, elle peut créer un objet
 * EventEmitter interne, en se spécifiant comme source d'événement et relayer
 * les appels aux méthodes add/removeListener à cet objet interne.
 */
public interface EventEmitterInterface {
	public abstract void addListener(EventListener listener);
	public abstract void removeListener(EventListener listener);
}
