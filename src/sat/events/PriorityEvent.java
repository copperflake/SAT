package sat.events;

/**
 * Un événement avec gestion de priorité. Ces événements peuvent être utilisé
 * avec un PriorityEventScheduler. Dans un tel cas, les événements seront
 * traité selon leur ordre de tri naturel.
 */
@SuppressWarnings("serial")
public abstract class PriorityEvent<T extends PriorityEvent<T>> extends Event implements Comparable<T> {
}
