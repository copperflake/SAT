package sat.utils.routes;

import java.io.Serializable;

import sat.utils.geo.Coordinates;

public class Waypoint implements Serializable {
	private MoveType type;
	private float[] args;
	
	public Waypoint(MoveType type, float[] args) {
		this.type = type;
		this.args = args;
	}
	
	public MoveType getType() {
		return type;
	}
	
	public Coordinates getCoordiates() {
		return new Coordinates(args[0], args[1], args[2]);
	}
	
	public float getRadius() {
		return args[3];
	}
}
