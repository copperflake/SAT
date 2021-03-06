package sat.tower.agent;

import java.util.HashMap;

import sat.events.Event;
import sat.events.EventEmitter;
import sat.events.EventListener;
import sat.tower.Tower;

public class TowerAgent extends EventEmitter implements EventListener {
	private TowerAgentExecutor executor;

	private int requestId;
	private HashMap<Integer, EventListener> pendingRequests;

	public TowerAgent() {
		executor = new TowerAgentExecutor();
		Tower.getInstance().addListener(this);

		requestId = 0;
		pendingRequests = new HashMap<Integer, EventListener>();
	}

	public void execute(AgentRequest req, EventListener handler) {
		if(handler != null) {
			synchronized(pendingRequests) {
				pendingRequests.put(++requestId, handler);
				req.setRequestID(requestId);
			}
		}

		execute(req);
	}

	public void execute(AgentRequest req) {
		try {
			req.trigger(executor);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	public void handleResult(AgentResult ev) {
		synchronized(pendingRequests) {
			int id = ev.getRequestID();
			if(pendingRequests.containsKey(id)) {
				EventListener handler = pendingRequests.remove(id);
				try {
					ev.trigger(handler);
				}
				catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void exit() {
		Tower.getInstance().removeListener(this);
	}

	public boolean isRemote() {
		return false;
	}

	// - - - Proxy - - -

	public void on(AgentResult ev) {
		if(ev.isRemoteRequest()) {
			emit(ev);
		}
		else {
			handleResult(ev);
		}
	}

	public void on(Event event) {
		emit(event);
	}

	// - - - API - - - 

	public void requestInit() {
		execute(new AgentRequest.Init(), null);
	}

	public void requestConfig(EventListener handler) {
		execute(new AgentRequest.Config(), handler);
	}

	public void requestConfigGetKey(String key, EventListener handler) {
		execute(new AgentRequest.ConfigGetKey(key), handler);
	}

	public void requestConfigSetKey(String key, String value) {
		execute(new AgentRequest.ConfigSetKey(key, value), null);
	}

	public void requestTowerKey(EventListener handler) {
		execute(new AgentRequest.TowerKey(), handler);
	}

	public void requestChoke() {
		execute(new AgentRequest.Choke(), null);
	}

	public void requestUnchoke() {
		execute(new AgentRequest.Unchoke(), null);
	}

	// - - - Executor - - -

	public class TowerAgentExecutor implements EventListener {
		private Tower tower;

		public TowerAgentExecutor() {
			tower = Tower.getInstance();
		}

		public void on(AgentRequest.Init ev) {
			tower.init();
		}

		public void on(AgentRequest.Config ev) {
			done(ev, new AgentResult.ConfigResult(tower.getConfig()));
		}

		public void on(AgentRequest.ConfigGetKey ev) {
			done(ev, new AgentResult.ConfigGetKeyResult(tower.getConfig().getString(ev.getKey())));
		}

		public void on(AgentRequest.ConfigSetKey ev) {
			tower.getConfig().setProperty(ev.getKey(), ev.getValue());
		}

		public void on(AgentRequest.TowerKey ev) {
			done(ev, new AgentResult.TowerKeyResult(tower.getKeyPair().getPublicKey()));
		}

		public void on(AgentRequest.Choke ev) {
			tower.choke();
		}

		public void on(AgentRequest.Unchoke ev) {
			tower.unchoke();
		}

		public void done(AgentRequest req, AgentResult ev) {
			ev.setRequestID(req.getRequestID());
			ev.setRemoteRequest(req.isRemoteRequest());

			try {
				ev.trigger(TowerAgent.this);
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
}
