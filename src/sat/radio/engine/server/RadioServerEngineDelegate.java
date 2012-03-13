package sat.radio.engine.server;

import sat.radio.RadioSocket;

public interface RadioServerEngineDelegate {
	public void onNewConnection(RadioSocket socket);
}
