package sat.radio;

import sat.events.Event;
import sat.radio.message.Message;

@SuppressWarnings("serial")
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

	public static abstract class MessageEvent extends RadioEvent {
		private Message message;

		public MessageEvent(Message m) {
			message = m;
		}

		public Message getMessage() {
			return message;
		}
	}

	public static class MessageSent extends MessageEvent {
		public MessageSent(Message m) {
			super(m);
		}
	}

	public static class MessageReceived extends MessageEvent {
		public MessageReceived(Message m) {
			super(m);
		}
	}

	// - - - Tower Event ( Tower -> Radio ) - - -

	public static abstract class TowerRadioEvent extends RadioEvent {

	}

	public static class TowerConnected extends TowerRadioEvent {

	}

	public static class TowerDisconnected extends TowerRadioEvent {

	}

	// - - - Plane Event ( Radio -> Tower ) - - -

	public static abstract class PlaneRadioEvent extends RadioEvent {
		RadioID id;

		public PlaneRadioEvent(RadioID id) {
			this.id = id;
		}

		public RadioID getID() {
			return id;
		}
	}

	public static class PlaneConnected extends PlaneRadioEvent {
		public PlaneConnected(RadioID id) {
			super(id);
		}
	}

	public static class PlaneDisconnected extends PlaneRadioEvent {
		public PlaneDisconnected(RadioID id) {
			super(id);
		}
	}
}
