package sat.radio.message;

import sat.radio.RadioID;
import sat.utils.routes.MoveType;
import sat.utils.routes.RoutingType;
import sat.utils.routes.Waypoint;

@SuppressWarnings("serial")
public class MessageRouting extends Message {
	private RoutingType routingType;
	private Waypoint waypoint;

	public MessageRouting(RadioID id, Waypoint waypoint, RoutingType routingType) {
		super(id, waypoint.getCoordiates());

		type = MessageType.ROUTING;
		priority = 2;

		this.routingType = routingType;
		this.waypoint = waypoint;

		if(waypoint.getType() == MoveType.CIRCULAR) {
			length = 4;
		}
	}

	public Waypoint getWaypoint() {
		return waypoint;
	}

	public RoutingType getRoutingType() {
		return routingType;
	}

	public String toString() {
		return "I'm a routing!";
	}
}
