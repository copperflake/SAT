package sat.radio;

import sat.events.Event;

public abstract class RadioEvent extends Event {

	public static class UncaughtException extends RadioEvent {
		private String message;
		private Throwable exception;

		public UncaughtException(String m, Throwable e) {
			message = m;
			exception = e;
		}

		public String getMessage() {
			return message;
		}

		public Throwable getException() {
			return exception;
		}
	}

	// - - - Plane Event - - -

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
	
	public static class PlaneMoved extends PlaneEvent {
		public PlaneMoved(RadioID id) {
			super(id);
		}
	}
}
