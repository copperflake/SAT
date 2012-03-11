package sat.radio;

import java.io.Serializable;

public class RadioID implements Serializable {
	private String name;
	
	public RadioID(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
	
	private static final long serialVersionUID = 6714099615154964027L;
}
