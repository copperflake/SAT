package sat.radio.engine.server;

import sat.radio.socket.RadioSocket;

public interface RadioServerEngineDelegate {
	public void onNewConnection(RadioSocket socket);
}
