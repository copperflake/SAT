package sat.tower.agent;

import sat.events.Event;
import sat.utils.cli.Config;
import sat.utils.crypto.RSAKey;

@SuppressWarnings("serial")
public abstract class AgentResult extends Event {
	private int requestID = 0;
	private boolean remoteRequest = false;

	public void setRequestID(int id) {
		requestID = id;
	}

	public int getRequestID() {
		return requestID;
	}

	public void setRemoteRequest(boolean remoteRequest) {
		this.remoteRequest = remoteRequest;
	}

	public boolean isRemoteRequest() {
		return remoteRequest;
	}

	// - - - Concrete Result - - -

	public static class ConfigResult extends AgentResult {
		private Config config;

		public ConfigResult(Config conf) {
			config = conf;
		}

		public Config getConfig() {
			return config;
		}
	}

	public static class ConfigGetKeyResult extends AgentResult {
		private String value;

		public ConfigGetKeyResult(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}
	}

	public static class TowerKeyResult extends AgentResult {
		private RSAKey key;

		public TowerKeyResult(RSAKey key) {
			this.key = key;
		}

		public RSAKey getKey() {
			return key;
		}
	}
}
