package sat.radio.server;

import sat.radio.RadioDelegate;
import sat.radio.RadioID;

public interface RadioServerDelegate extends RadioDelegate {
	public void onPlaneConnected(RadioID plane);
	public void onPlaneDisconnected(RadioID plane);
}
