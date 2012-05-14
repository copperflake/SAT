package sat.radio;

import sat.events.Event;

public abstract class RadioEvent extends Event {
	public static abstract class PlaneEvent extends RadioEvent {
		RadioID id;

		public PlaneEvent(RadioID id) {
			this.id = id;
		}

		public RadioID getId() {
			return id;
		}
	}

	public static class PlaneConnected extends PlaneEvent {
		public PlaneConnected(RadioID id) {
			super(id);
		}
	}

	public static class PlaneDisconnected extends PlaneEvent {
		public PlaneDisconnected(RadioID id) {
			super(id);
		}
	}
}
