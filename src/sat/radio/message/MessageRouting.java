package sat.radio.message;

import sat.plane.Route;
import sat.radio.RadioID;
import sat.utils.geo.Coordinates;

public class MessageRouting extends Message {
	Route route;

	public MessageRouting(RadioID id, Coordinates c, Route route) {
		super(id, c);

		this.route = route;
	}

	public void setTypeAndPriority() {
		type = MessageType.ROUTING;
		priority = 2;
	}

	public String toString() {
		return "I'm a routing!";
	}

	private static final long serialVersionUID = -8493801549147991470L;
}
