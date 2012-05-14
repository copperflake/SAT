package sat.radio.engine.server;

import java.io.IOException;

import sat.radio.engine.RadioEngine;

public abstract class RadioServerEngine extends RadioEngine {
	protected RadioServerEngineDelegate delegate;

	public abstract void init(RadioServerEngineDelegate delegate) throws IOException;
}
