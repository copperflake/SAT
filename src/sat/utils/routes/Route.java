package sat.utils.routes;

import java.util.ArrayList;

public class Route extends ArrayList<Waypoint> {
	private int capacity;
	private boolean landing = false;

	public Route(int capacity) {
		this.capacity = capacity;
	}

	public boolean isLanding() {
		return landing;
	}
	
	public void setLanding() {
		landing = true;
	}

	public int getCapacity() {
		return (landing) ? 1 : capacity;
	}
}
