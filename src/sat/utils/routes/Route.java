package sat.utils.routes;

import java.util.ArrayList;

@SuppressWarnings("serial")
public class Route extends ArrayList<Waypoint> implements Cloneable {
	private int capacity;
	private boolean landing = false;

	public Route() {
		this(-1);
	}

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
