package sat.events;

import java.util.ArrayDeque;
import java.util.Queue;

public class AsyncEventEmitter<T extends EventListener> extends EventEmitter<T> {
	private Queue<Runnable> eventsQueue = new ArrayDeque<Runnable>();
	private Runnable activeEvent;
	
	protected synchronized void emit(final Event<T> event) {
		eventsQueue.offer(new Runnable(){
			public void run() {
				try {
					AsyncEventEmitter.super.emit(event);
				} finally {
					emitNext();
				}
			}
		});
		
		if(activeEvent == null) {
			emitNext();
		}
	}
	
	private synchronized void emitNext() {
		if((activeEvent = eventsQueue.poll()) != null) {
            new Thread(activeEvent).start();
        }
	}
}
