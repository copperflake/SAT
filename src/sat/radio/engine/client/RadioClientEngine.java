package sat.radio.engine.client;

import java.io.IOException;

import sat.radio.RadioClient;
import sat.radio.engine.RadioEngine;

public abstract class RadioClientEngine extends RadioEngine {
	protected RadioClient radioClient;

	public abstract void init(RadioClientEngineDelegate delegate) throws IOException;
}
