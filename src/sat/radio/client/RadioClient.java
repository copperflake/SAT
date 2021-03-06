package sat.radio.client;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import sat.events.Event;
import sat.events.EventListener;
import sat.events.UnhandledEventException;
import sat.radio.Radio;
import sat.radio.RadioEvent;
import sat.radio.RadioID;
import sat.radio.engine.client.RadioClientEngine;
import sat.radio.message.*;
import sat.radio.socket.RadioSocket;
import sat.radio.socket.RadioSocketState;
import sat.utils.crypto.RSAInputStream;
import sat.utils.crypto.RSAKey;
import sat.utils.crypto.RSAKeyPair;
import sat.utils.crypto.RSAOutputStream;
import sat.utils.file.DataFile;
import sat.utils.geo.Coordinates;

/**
 * Un client radio.
 * <p>
 * Un client radio est capable de se connecter à un seveur radio pour
 * communiquer.
 * <p>
 * À la différence d'un serveur radio, le client radio ne peut pas accepter de
 * connexion entrante et n'est pas capable de se connecter à plus d'un hôte.
 */
public class RadioClient extends Radio {
	/**
	 * Le délégué de la radio. Le délégué reçoit les évenements émis par la
	 * radio et est chargé de leur gestion.
	 */
	private RadioClientDelegate delegate;

	/**
	 * Le moteur de communication de la radios.
	 */
	private RadioClientEngine engine;

	/**
	 * Le socket de communication vers la tour.
	 */
	private RadioSocket socket;

	/**
	 * Le thread d'écoute d'entrée.
	 */
	private TowerSocketManager manager;

	/**
	 * Crée un client radio utilisant le délégué <code>delegate</code> pour
	 * gérer les évenements.
	 * 
	 * @param delegate
	 *            Le délégué chargé de la gestion des événements.
	 */
	public RadioClient(RadioClientDelegate delegate, RadioID id) {
		super(delegate, id);
		this.delegate = delegate;
	}

	/**
	 * Connecte le client radio à un serveur radio en utilisant le moteur de
	 * communcation <code>engine</code> spécifié. Le moteur passé en paramètre
	 * est automatiquement initialisé.
	 * 
	 * @param engine
	 *            Le moteur de communication utilisé pour la connexion
	 * 
	 * @throws IOException
	 *             L'initialisation du moteur de communication peut provoquer
	 *             une exception qui est passée au code appelant. De plus, si
	 *             cette radio est déjà associée à un moteur de communication,
	 *             une exception sera levée.
	 */
	public void connect(RadioClientEngine engine) throws IOException {
		// Un client radio ne peut avoir qu'un seul moteur actif.
		if(this.engine != null) {
			throw new IOException("This radio already have a registered engine.");
		}

		try {
			this.engine = engine;
			socket = this.engine.init();
		}
		catch(IOException e) {
			// Reset the engine if initialization was not successful.
			this.engine = null;

			// Rethrow exception
			throw e;
		}

		manager = new TowerSocketManager(socket);

		Coordinates coords = delegate.getLocation();
		manager.send(new MessageHello(id, coords, ciphered, !legacy));
	}
	
	/**
	 * Quitte la radio de manière propre.
	 */
	public void quit() {
		manager.quit();
	}

	/**
	 * Envoi un message quelconque à la tour. Méthode interne.
	 */
	private void send(Message message) {
		manager.send(message);
	}

	/**
	 * Envoi un KeepAlive à la tour.
	 */
	public void sendKeepalive() {
		send(new MessageKeepalive(id, delegate.getLocation()));
	}

	/**
	 * Envoi un fichier à la tour.
	 */
	public void sendFile(DataFile file) {
		manager.sendFile(file);
	}

	/**
	 * Envoi un message textuel à la tour.
	 */
	public void sendText(String text) {
		manager.sendText(text);
	}

	/**
	 * Envoi une demande d'atterissage à la tour.
	 */
	public void sendLandingRequest() {
		send(new MessageLanding(id, delegate.getLocation()));
	}

	/**
	 * Annonce une situation d'urgence à la tour.
	 */
	public void sendMayDay(String info) {
		send(new MessageMayDay(id, delegate.getLocation(), info));
	}

	/**
	 * Remercie la tour de sa collaboration.
	 */
	public void sendBye() {
		send(new MessageBye(id, delegate.getLocation()));
	}

	/**
	 * Choke ou Unchoke le client.
	 * 
	 * @param choked
	 */
	public void setChoked(boolean choked) {
		manager.setChoked(choked);
	}

	// - - - Tower Socket Manager - - -

	/**
	 * Objet de gestion de socket vers la tour.
	 */
	public class TowerSocketManager extends SocketManager {
		/**
		 * Gestionnaire de messages.
		 */
		private MessageHandler messageHandler;

		public TowerSocketManager(RadioSocket socket) {
			super(socket);

			messageHandler = new MessageHandler();

			start();
		}

		protected void handleMessage(Message message) throws InvocationTargetException, UnhandledEventException {
			message.trigger(messageHandler);
		}

		protected void emitEvent(Event event) {
			emit(event);
		}

		protected void ready() {
			super.ready();

			emit(new RadioEvent.TowerConnected());
		}

		protected void quit() {
			super.quit();
			emit(new RadioEvent.TowerDisconnected());
		}

		public class MessageHandler implements EventListener {
			public void on(MessageHello m) {
				// HELLO can be received only when in HANDSHAKE state
				if(state != RadioSocketState.HANDSHAKE) {
					invalidState(m);
				}

				// Extended use a specific handshake
				if(m.isExtended()) {
					listener.upgrade();
					writer.upgrade();

					state = RadioSocketState.EXTENDED_HANDSHAKE;

					Coordinates coords = delegate.getLocation();
					writer.send(new MessageUpgrade(id, coords));

					if(!m.isCiphered()) {
						ready();
					}

					return;
				}

				if(m.isCiphered()) {
					upgradeCipher(delegate.getLegacyTowerKey().getPublicKey());
				}

				ready();
			}

			public void on(MessageSendRSAKey m) {
				if(state != RadioSocketState.EXTENDED_HANDSHAKE) {
					invalidState(m);
				}

				upgradeCipher(m.getKey());
				ready();
			}

			// Unmanaged messages type
			public void on(Message m) {
				// If not in READY state, the message must be a
				// protocol message.
				// Since all protocol messages are short-circuited,
				// and we are in the default case, it's
				// obviously not a protocol message.
				if(state != RadioSocketState.READY) {
					invalidState(m);
				}

				forwardToPlane(m);
			}
		}

		private void upgradeCipher(RSAKey towerPubKey) {
			RSAKeyPair planeKey = delegate.getKeyPair();
			RSAKeyPair towerKey = new RSAKeyPair(towerPubKey);

			socket.in.upgrade(new RSAInputStream(socket.in.getStream(), planeKey));
			socket.out.upgrade(new RSAOutputStream(socket.out.getStream(), towerKey));

			Coordinates coords = delegate.getLocation();
			writer.send(new MessageSendRSAKey(id, coords, delegate.getKeyPair().getPublicKey()));
		}

		private void forwardToPlane(Message m) {
			RadioClient.this.emit(m);
		}

		private void invalidState(Message m) {
			// Invalid message for this state, disconnect plane.
			System.err.println("Protocol Exception from Tower");
			System.err.println("Cannot receive " + m.getType() + " in state " + state);
			quit();
		}
	}
}
