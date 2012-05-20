package sat;

import sat.events.Event;

public class DebugEvent extends Event {
	public String message;
	
	public DebugEvent(String msg) {
		message = msg;
	}
	
	public String getMessage() {
		return message;
	}
}
