package sat.utils.geo;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Coordinates implements Serializable {
	private float x;
	private float y;
	private float z;

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
}
