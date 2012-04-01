package sat.radio.server;

import java.io.EOFException;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.PriorityBlockingQueue;

import sat.radio.Radio;
import sat.radio.RadioID;
import sat.radio.RadioProtocolException;
import sat.radio.engine.server.RadioServerEngine;
import sat.radio.engine.server.RadioServerEngineDelegate;
import sat.radio.message.Message;
import sat.radio.message.MessageHello;
import sat.radio.message.MessageSendRSAKey;
import sat.radio.message.stream.UpgradableMessageInputStream;
import sat.radio.message.stream.UpgradableMessageOutputStream;
import sat.radio.socket.RadioSocket;
import sat.radio.socket.RadioSocketState;
import sat.utils.crypto.RSAInputStream;
import sat.utils.crypto.RSAKeyPair;
import sat.utils.crypto.RSAOutputStream;
import sat.utils.geo.Coordinates;

/**
 * Un serveur radio.
 * <p>
 * Un serveur radio écoute un flux d'entrée en attente de client radio. Il
 * utilise en interne un moteur de serveur radio qui s'occupe de gérer la partie
 * technique des communications.
 */
public class RadioServer extends Radio {
	/**
	 * Le délégué de ce serveur radio, il sera responsable de gérer les
	 * événements emis par la radio au cours de son fonctionnement.
	 */
	private RadioServerDelegate delegate;

	/**
	 * Le moteur de serveur radio utilisé.
	 */
	// TODO: multiple engine
	private RadioServerEngine engine;

	/**
	 * File d'attente des messages entrants.
	 */
	private PriorityBlockingQueue<Message> incomingMessages;

	/**
	 * Thread de gestion des messages entrants.
	 */
	private IncomingMessageEmitter incomingThread;

	/**
	 * Liste des pairs connectés avec le gestionnaire associé.
	 */
	private HashMap<RadioID, SocketManager> managers;

	/**
	 * Crée un nouveau serveur radio.
	 * 
	 * @param delegate
	 *            Le délégué qui sera chargé de la gestion des événements de la
	 *            radio.
	 */
	public RadioServer(RadioServerDelegate delegate, String label) {
		super(label, delegate.getConfig().getInt("radio.keylength"));

		this.delegate = delegate;

		this.managers = new HashMap<RadioID, SocketManager>();

		incomingMessages = new PriorityBlockingQueue<Message>();

		incomingThread = new IncomingMessageEmitter();
		incomingThread.start();
	}

	/**
	 * Défini et initialise le moteur d'écoute de ce serveur radio.
	 * <p>
	 * Cette fonction est sans effet si le moteur a déjà été défini.
	 * 
	 * @param engine
	 *            Le moteur de serveur radio à utiliser.
	 * 
	 * @throws IOException
	 *             L'initialisation du moteur peut provoquer une exception qui
	 *             est passée au code parent.
	 */
	public void listen(RadioServerEngine engine) throws IOException {
		// TODO multiples engines !
		if(this.engine != null)
			return;

		this.engine = engine;
		this.engine.init(new Delegate());
	}

	// - - - Engine Events Delegate - - -

	private class Delegate implements RadioServerEngineDelegate {
		/**
		 * Gestion de la connexion d'un nouveau client.
		 */
		public void onNewConnection(RadioSocket socket) {
			// Lancement d'un SocketManager qui s'occupera de ce client
			new SocketManager(socket).start();
		}
	}

	// - - - Event Emitter - - -

	/**
	 * Gestionnaire de la file d'attente de message entrant. Ce thread écoute la
	 * queue incomingMessages et notifie le délégué de la radio de l'arrivée de
	 * nouveaux messages.
	 */
	private class IncomingMessageEmitter extends Thread {
		private boolean running = true;

		public void run() {
			while(running) {
				Message message;
				try {
					message = incomingMessages.take();
					delegate.onMessage(message);
				}
				catch(InterruptedException e) {
				}
			}
		}

		/**
		 * Arrête le thread de notification.
		 */
		public void quit() {
			running = false;
		}
	}

	// - - - Socket Manager - - -

	/**
	 * Un gestionnaire de socket.
	 */
	private class SocketManager {
		/**
		 * Le socket géré par ce gestionnaire.
		 */
		private RadioSocket socket;

		/**
		 * L'id de l'avion auquel correspond ce socket.
		 */
		private RadioID socketID;

		/**
		 * Le thread d'écoute d'entrée.
		 */
		private SocketListener listener;

		/**
		 * Le thread d'écoute de sortie.
		 */
		private SocketWriter writer;

		/**
		 * État de cette connexion avec l'avion.
		 */
		private RadioSocketState state = RadioSocketState.HANDSHAKE;

		/**
		 * Indique si ce manager s'occupe d'un client utilisant le protocole
		 * étendu plutôt que le protocole ITP.
		 */
		private boolean extended = false;

		/**
		 * Indique si ce socket est sécurisé.
		 */
		private boolean ciphered = false;

		/**
		 * Crée un gestionnaire de socket.
		 * 
		 * @param socket
		 *            Le socket à gérer.
		 */
		public SocketManager(RadioSocket socket) {
			this.socket = socket;

			listener = new SocketListener();
			writer = new SocketWriter();
		}

		public boolean isExtended() {
			return extended;
		}

		public void start() {
			listener.start();
			writer.start();
		}

		private void upgrade() {
			try {
				listener.upgrade();
				writer.upgrade();
			}
			catch(IOException e) {
				// Failed to upgrade streams
				e.printStackTrace();
				quit();
			}
		}

		private void ready() {
			synchronized(managers) {
				managers.put(socketID, this);
			}

			state = RadioSocketState.READY;
			delegate.onPlaneConnected(socketID);
		}

		private void quit() {
			// Unregister
			if(state == RadioSocketState.READY) {
				synchronized(managers) {
					managers.remove(socketID);
				}

				delegate.onPlaneDisconnected(socketID);
			}

			try {
				socket.close();
			}
			catch(IOException e) {
				System.err.println("Error while closing socket!?");
				e.printStackTrace(System.err);
			}

			listener.quit();
			writer.quit();
		}

		// - - - Listener - - -

		/**
		 * Thread de gestion de l'entrée du socket.
		 */
		private class SocketListener extends Thread {
			/**
			 * Flux d'entrée de messages.
			 */
			private UpgradableMessageInputStream mis;

			/**
			 * État du thread.
			 */
			private boolean running = true;

			/**
			 * Crée un nouveau thread d'écoute d'entrée client.
			 */
			public SocketListener() {
				mis = new UpgradableMessageInputStream(socket.in);
			}

			/**
			 * Méthode principale du thread.
			 */
			public void run() {
				try {
					Message message;

					while(running) {
						synchronized(mis) {
							message = mis.readMessage();
						}

						handleMessage(message);
					}
				}
				catch(EOFException e) {
					SocketManager.this.quit();
				}
				catch(RadioProtocolException e) {
					// Invalid message for this state, disconnect plane.
					System.err.println("Protocol Exception from Plane");
					e.printStackTrace(System.err);
					SocketManager.this.quit();
				}
				catch(IOException e) {
					// Unable to read message, disconnect plane.
					System.err.println("Cannot read from plane socket");
					e.printStackTrace(System.err);
					SocketManager.this.quit();
				}
			}
			/**
			 * Gestion du court-circuitage des messages protocolaires.
			 * 
			 * @throws RadioProtocolException
			 *             Si le message reçu n'est pas autorisé dans l'état
			 *             actuel de la connexion.
			 */
			private void handleMessage(Message m) throws RadioProtocolException {
				switch(m.getType()) {
					case HELLO:
						// HELLO can be received only when in HANDSHAKE state
						if(state != RadioSocketState.HANDSHAKE) {
							throwInvalidState(m);
						}

						handleMessage((MessageHello) m);
						break;

					case SENDRSA:
						// SENDRSA can be received only when in
						// CIPHER_NEGOCIATION state
						if(state != RadioSocketState.CIPHER_NEGOCIATION) {
							throwInvalidState(m);
						}

						handleMessage((MessageSendRSAKey) m);
						break;

					default:
						// If not in READY state, the message must be a
						// protocol message.
						// Since all protocol messages are short-circuited in
						// this switch, and we are in the default case, it's
						// obviously not a protocol message.
						if(state != RadioSocketState.READY) {
							throwInvalidState(m);
						}

						// No short circuit, send it to incoming queue
						incomingMessages.put(m);
				}
			}

			/**
			 * Méthode utilitaire pour lancer les exceptions au protocole
			 * lorsque le message reçu ne correspond pas à celui attendu par
			 * rapport à l'état actuel de la connexion.
			 * 
			 * @param m
			 *            Le message reçu. Utilisé pour récupérer son type.
			 * 
			 * @throws RadioProtocolException
			 *             L'exception générée.
			 */
			private void throwInvalidState(Message m) throws RadioProtocolException {
				throw new RadioProtocolException("Cannot receive " + m.getType() + " in state " + state);
			}

			/**
			 * Gestion du message Hello.
			 */
			private void handleMessage(MessageHello m) {
				// Enable extended protocol if not disabled
				if(!delegate.getConfig().getBoolean("radio.legacy")) {
					extended = m.isExtended();
				}

				// Enable encryption
				if(delegate.getConfig().getBoolean("radio.ciphered")) {
					ciphered = m.isCiphered();
				}

				Coordinates coords = delegate.getLocation();
				writer.send(new MessageHello(id, coords, ciphered, extended));

				socketID = m.getID();

				if(extended) {
					// If we use the extended protocol, we can upgrade
					// components and wait for the extended handshake.
					SocketManager.this.upgrade();
				}
				else if(ciphered) {
					state = RadioSocketState.CIPHER_NEGOCIATION;
					socket.in.upgrade(new RSAInputStream(socket.in.getStream(), keyPair));
				}
				else {
					// Socket is ready!
					SocketManager.this.ready();
				}
			}

			/**
			 * Gestion du message SendRSAKey
			 */
			public void handleMessage(MessageSendRSAKey m) {
				// Upgrade the output stream to write encrypted data with the
				// plane public key.
				RSAKeyPair planeKey = new RSAKeyPair(m.getKey());
				socket.out.upgrade(new RSAOutputStream(socket.out.getStream(), planeKey));

				// Socket is ready for general usage.
				SocketManager.this.ready();
			}

			/**
			 * Upgrade le flux de lecture sous-jacent. Le nouveau flux sera de
			 * type ExtendedMessageInputStream.
			 * 
			 * @throws IOException
			 *             Si l'upgrade du flux a généré une exception.
			 */
			public void upgrade() throws IOException {
				synchronized(mis) {
					mis.upgrade();
				}
			}

			/**
			 * Arrête le thread d'écoute.
			 */
			public void quit() {
				running = false;
				this.interrupt();
			}
		}

		// - - - Writer - - -

		/**
		 * Thread de gestion de la liste d'attente des messages sortants.
		 */
		private class SocketWriter extends Thread {
			/**
			 * Flux de sortie des messages.
			 */
			private UpgradableMessageOutputStream mos;

			/**
			 * La file d'attente de messages à envoyer.
			 */
			private PriorityBlockingQueue<Message> queue = new PriorityBlockingQueue<Message>();

			/**
			 * État du thread.
			 */
			private boolean running = true;

			/**
			 * Crée un nouveau thread d'écrite vers un pair.
			 */
			public SocketWriter() {
				mos = new UpgradableMessageOutputStream(socket.out);
			}

			public void run() {
				Message message;

				while(running) {
					try {
						message = queue.take();
						synchronized(mos) {
							mos.writeMessage(message);
						}
					}
					catch(InterruptedException e) {
					}
					catch(IOException e) {
						System.err.println("Cannot write to plane socket");
						e.printStackTrace(System.err);

						// Unable to write message, disconnect plane.
						SocketManager.this.quit();
					}
				}
			}

			/**
			 * Envoie un message à l'avion.
			 * 
			 * @param m
			 *            Le message à envoyer.
			 */
			public void send(Message m) {
				queue.put(m);
			}

			/**
			 * Upgrade le flux d'écriture sous-jacent. Le nouveau flux sera de
			 * type ExtendedMessageOutputStream.
			 * 
			 * @throws IOException
			 *             Si l'upgrade du flux a généré une exception.
			 */
			public void upgrade() throws IOException {
				// Ensure the message output stream is not in use
				// when upgrading.
				synchronized(mos) {
					mos.upgrade();
				}
			}

			/**
			 * Arrête le thread.
			 */
			public void quit() {
				running = false;
				this.interrupt();
			}
		}
	}
}
