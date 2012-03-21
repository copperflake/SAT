package sat.radio.message;

import sat.plane.Route;
import sat.radio.RadioID;

public class MessageRouting extends Message {
	protected MessageType type = MessageType.ROUTING;
	protected int priority = 2;

	Route route;

	public MessageRouting(RadioID id, int px, int py, Route route) {
		super(id, px, py);
		this.route = route;
	}

	public String toString() {
		return "I'm a routing!";
	}

	private static final long serialVersionUID = -8493801549147991470L;
}
