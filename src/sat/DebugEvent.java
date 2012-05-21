package sat;

import sat.events.Event;

@SuppressWarnings("serial")
public class DebugEvent extends Event {
	public String message;
	
	public DebugEvent(String msg) {
		message = msg;
	}
	
	public String getMessage() {
		return message;
	}
}
