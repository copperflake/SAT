package sat.tower.agent;

import java.lang.reflect.InvocationTargetException;

import sat.events.Event;
import sat.events.EventEmitter;
import sat.events.EventListener;
import sat.events.UnhandledEventException;
import sat.tower.Tower;

public class TowerAgent extends EventEmitter implements EventListener {
	private TowerAgentExecutor executor;

	public TowerAgent() {
		executor = new TowerAgentExecutor();
		Tower.getInstance().addListener(this);
	}

	public void execute(AgentRequest req) {
		try {
			req.notify(executor);
		}
		catch(InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch(UnhandledEventException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void exit() {
		Tower.getInstance().removeListener(this);
	}

	// - - - Proxy - - -

	public void on(Event event) {
		emit(event);
	}

	// - - - API - - - 

	public void requestPlanes() {

	}

	// - - - Executor - - -

	public class TowerAgentExecutor implements EventListener {

	}
}
