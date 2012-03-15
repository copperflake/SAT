package sat.radio.message;

import sat.plane.Route;

public class MessageRouting extends Message {
	protected MessageType type = MessageType.ROUTING;
	protected int priority = 0;
	
	Route route;
	public MessageRouting(Route route) {
		super();
		this.route = route;
	}
	
	public String toString() {
		return "I'm a routing!";
	}
}
