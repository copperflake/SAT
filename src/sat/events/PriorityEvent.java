package sat.events;

public abstract class PriorityEvent<T extends PriorityEvent<T>> extends Event implements Comparable<T> {

}
