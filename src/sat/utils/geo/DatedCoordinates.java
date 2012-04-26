package sat.utils.geo;

import java.util.Date;

public class DatedCoordinates {
	private Date date;
	private FloatCoordinates coords;
	
	public DatedCoordinates(FloatCoordinates coords, Date date) {
		this.coords = coords;
		this.date = date;
	}
	
	public Date getDate() {
		return this.date;
	}

	public FloatCoordinates getCoordinates() {
		return this.coords;
	}
}