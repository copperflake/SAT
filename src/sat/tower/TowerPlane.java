package sat.tower;

import sat.plane.PlaneType;
import sat.radio.RadioID;

public class TowerPlane {
	private static int nextLandingID = 0;
	
	private synchronized static int getNextLandingID() {
		return nextLandingID++;
	}
	
	private RadioID id;
	private int landingID = -1;
	private int currentRoute = -1;
	private boolean mayDay = false;
	private PlaneType type;
	private boolean landing;
	
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
		if(!isLanding()) {
			this.mayDay = mayDay;
		}
	}

	public RadioID getID() {
		return id;
	}

	public int getLandingID() {
		return landingID;
	}
	
	public void landingRequested() {
		if(landingID == -1) {
			landingID = getNextLandingID();
		}
	}

	public PlaneType getType() {
		return type;
	}

	public void setType(PlaneType type) {
		if(!isLanding()) {
			this.type = type;
		}
	}

	public boolean isLanding() {
		return landing;
	}

	public void setLanding() {
		this.landing = true;
	}
}
