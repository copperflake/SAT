package sat.tower.events;

import sat.events.Event;
import sat.tower.Tower;

public abstract class TowerEvent extends Event<TowerEventListener, Tower> {
	public TowerEvent(Tower emitter) {
		super(emitter);
	}

	public static class PlaneConnected extends TowerEvent {
		public PlaneConnected(Tower emitter) {
			super(emitter);
		}

		public void notify(TowerEventListener listener) {
			listener.on(this);
		}
	}
}
