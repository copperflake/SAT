package sat.radio.engine.client;

import java.io.IOException;

import sat.radio.engine.RadioEngine;
import sat.radio.socket.RadioSocket;

public abstract class RadioClientEngine extends RadioEngine {
	protected RadioClientEngineDelegate delegate;

	public abstract RadioSocket init(RadioClientEngineDelegate delegate) throws IOException;

	public RadioClientEngineDelegate getDelegate() {
		return delegate;
	}

	public void setDelegate(RadioClientEngineDelegate delegate) {
		this.delegate = delegate;
	}
}
