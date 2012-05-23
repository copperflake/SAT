package sat.tower;

import java.util.Date;

import sat.plane.PlaneType;
import sat.radio.RadioID;

public class TowerPlane {
	private RadioID id;
	private Date landingRequested;
	private int currentRoute = -1;
	private boolean mayDay = false;
	private PlaneType type;
	
	public TowerPlane(RadioID id) {
		this.id = id;
	}

	public int getCurrentRoute() {
		return currentRoute;
	}

	public void setCurrentRoute(int currentRoute) {
		this.currentRoute = currentRoute;
	}

	public boolean isMayDay() {
		return mayDay;
	}

	public void setMayDay(boolean mayDay) {
		this.mayDay = mayDay;
	}

	public RadioID getID() {
		return id;
	}

	public Date getLandingRequested() {
		return landingRequested;
	}
	
	public void landingRquested() {
		landingRequested = new Date();
	}

	public PlaneType getType() {
		return type;
	}

	public void setType(PlaneType type) {
		this.type = type;
	}
}
