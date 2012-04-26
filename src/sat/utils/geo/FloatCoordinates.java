package sat.utils.geo;

public class FloatCoordinates {
	private float x;
	private float y;
	private float z;
	
	public FloatCoordinates(float x, float y, float z) {
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
	
	public boolean equals(FloatCoordinates obj) {
		if(obj.getX() == this.x && obj.getY() == this.y && obj.getZ() == this.z)
			return true;
		return false;
	}
}
