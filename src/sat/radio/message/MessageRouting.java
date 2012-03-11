package sat.radio.message;

import sat.plane.Route;

public class MessageRouting extends Message {
	Route route;
	public MessageRouting(Route route) {
		super();
		this.route = route;
	}
}
