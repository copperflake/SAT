package sat.plane;

public enum PlaneType {
	A320 		( 0.78f,	10000,	60,		179 ),
	A380 		( 0.89f,	80000,	115,	644 ),
	B787		( 0.85f,	15000,	63,		242 ),
	CONCORDE	( 2.02f,	120000,	461,	140 ),
	GRIPEN		( 2.00f,	45000,	200,	1	);
	
	public static PlaneType getPlaneTypeByName(String name) {
		if(name.equals("A320") || name.equals("a320")) {
			return A320;
		} else if(name.equals("A320") || name.equals("a320")) {
			return A320;
		} else if(name.equals("A380") || name.equals("a380")) {
			return A380;
		} else if(name.equals("B787") || name.equals("b787")) {
			return B787;
		} else if(name.equals("CONCORDE") || name.equals("Concorde") || name.equals("concorde")) {
			return CONCORDE;
		} else if(name.equals("GRIPEN") || name.equals("Gripen") || name.equals("gripen")) {
			return GRIPEN;
		} else {
			return null;
		}
	}
	
	public final float speed;
	public final int fuel, consumption, passengers;
	
	PlaneType(float speed, int fuel, int consumption, int passengers) {
		this.speed = speed;
		this.fuel = fuel;
		this.consumption = consumption;
		this.passengers = passengers;
	}
}
