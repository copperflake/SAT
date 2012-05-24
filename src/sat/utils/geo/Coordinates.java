package sat.utils.geo;

import java.io.Serializable;

/**
 * Coordonées
 */
@SuppressWarnings("serial")
public class Coordinates implements Serializable {
	private float x;
	private float y;
	private float z;
	
	public Coordinates(float x, float y) {
		this(x, y, -1f);
	}

	public Coordinates(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	public float getZ() {
		return z;
	}

	public boolean equals(Coordinates obj) {
		if(obj.getX() == this.x && obj.getY() == this.y && obj.getZ() == this.z)
			return true;

		return false;
	}
	
	public static Coordinates parseCoordinates(String coords) throws InvalidCoordinatesException {
		String[] parts = coords.split(",");
		
		if(parts.length == 2) {
			return new Coordinates(
				Float.parseFloat(parts[0]),
				Float.parseFloat(parts[1]),
				-1f
			);
		}
		
		if(parts.length == 3) {
			return new Coordinates(
				Float.parseFloat(parts[0]),
				Float.parseFloat(parts[1]),
				Float.parseFloat(parts[2])
			);
		}
		
		throw new InvalidCoordinatesException();
	}
	
	public String toString() {
		return x+","+y+","+z;
	}
	
	public float[] toFloats() {
		return new float[] {x, y, z};
	}
}
