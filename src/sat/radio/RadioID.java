package sat.radio;

import java.io.Serializable;
import java.util.Date;

/**
 * RadioID identify peers in Radio communication
 */
public class RadioID implements Serializable {
	private String label;
	private long time;
	private long id;
	
	static private final int TIME_DIGIT = 4;
	static private final int RAND_DIGIT = 4;

	static private final long TIME_POW = (long) Math.pow(10L, TIME_DIGIT);
	static private final long RAND_POW = (long) Math.pow(10L, RAND_DIGIT);
	static private final long GLOB_POW = (long) Math.pow(10L, TIME_DIGIT+RAND_DIGIT);
	
	
	public RadioID(String label) {
		this.label = label;
		
		Date now = new Date();
		time = (int) ((now.getTime()/1000)%TIME_POW);
		
		id = (int) (Math.round(Math.random()*RAND_POW)%RAND_POW);
	}
	
	public String toString() {
		String asString = new Long(GLOB_POW + time*RAND_POW + id).toString();
		
		String timeString = asString.substring(1, TIME_DIGIT+1);
		String randString = asString.substring(TIME_DIGIT+1);
		
		return label + "-" + timeString + "-" + randString;
	}
	
	private static final long serialVersionUID = 6714099615154964027L;
}
