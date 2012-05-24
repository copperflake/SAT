package sat.tower.agent;

import sat.events.Event;

@SuppressWarnings("serial")
public abstract class AgentRequest extends Event {
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

	// - - - Concrete Requests - - -

	public static class Init extends AgentRequest {
	}

	public static class Config extends AgentRequest {
	}

	public static class ConfigGetKey extends AgentRequest {
		private String key;

		public ConfigGetKey(String key) {
			this.key = key;
		}

		public String getKey() {
			return key;
		}
	}

	public static class ConfigSetKey extends AgentRequest {
		private String key;
		private String value;

		public ConfigSetKey(String key, String value) {
			this.key = key;
			this.value = value;
		}

		public String getKey() {
			return key;
		}

		public String getValue() {
			return value;
		}
	}

	public static class TowerKey extends AgentRequest {
	}

	public static class Choke extends AgentRequest {
	}

	public static class Unchoke extends AgentRequest {
	}
}
