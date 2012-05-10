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
import sat.radio.message.*;
import sat.radio.message.handler.MessageHandler;
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
 * repose sur un moteur de serveur radio qui s'occupe de gérer la partie
 * technique des communications.
 * <p>
 * Le serveur est conçu pour être integré dans un objet qui s'occupera de gérer
 * les différents évenement qui surviennent lors de l'execution. Cet objet est
 * appelé le <code>delegate</code>.
 */
public class RadioServer extends Radio {
	/**
	 * Le délégué de ce serveur radio, il sera responsable de gérer les
	 * événements emis par la radio au cours de son fonctionnement ainsi que de
	 * fournir certaines informations comme la position et la configuration.
	 */
	private RadioServerDelegate delegate;

	/**
	 * Le moteur de serveur radio utilisé.
	 */
	// TODO: multiple engine
	private RadioServerEngine engine;

	/**
	 * File d'attente des messages entrants. Chaque message entrant sera placé
	 * dans cette queue qui est lue par l'objet {@link IncomingMessageEmitter}.
	 */
	private PriorityBlockingQueue<Message> incomingMessages;

	/**
	 * Thread de gestion des messages entrants. Surveille la queue de messages
	 * entrants et se charge d'appeler la méthode <code>onMessage</code> du
	 * délégué.
	 */
	private IncomingMessageEmitter incomingThread;

	/**
	 * Liste des pairs connectés avec le gestionnaire associé. Chaque Manager a
	 * la responsabilité de s'ajouter dans cette liste lorsqu'il devient prêt à
	 * être utilisé.
	 */
	private HashMap<RadioID, SocketManager> managers;

	/**
	 * Crée un nouveau serveur radio qui dépend du délégué spécifié.
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
	 * Cette fonction est (pour l'instant) sans effet si le moteur a déjà été
	 * défini.
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

	/**
	 * Déconnecte de force un client de ce serveur. Cette méthode est utilisée
	 * par l'objet superviseur (dans notre cas la tour) pour déconnecter de
	 * force un avion en cas d'erreur.
	 * 
	 * @param id
	 *            Le RadioID du client à déconnecter.
	 */
	public void kick(RadioID id) {
		synchronized(managers) {
			if(managers.containsKey(id)) {
				managers.get(id).kick();
			}
		}
	}

	// - - - Engine Events Delegate - - -

	/**
	 * L'objet délégué qui sera passé au moteur de radio. Ces méthodes sont
	 * implémentées dans une classe imbriquée plutôt que directement dans la
	 * classe principale pour éviter d'exposer des méthodes théoriquement
	 * privées (puisqu'elle ne doivent pas être appelée par l'objet superviseur
	 * (la tour)) mais qui doivent être déclarées public pour pouvoir être
	 * appelées par le moteur.
	 * <p>
	 * Note: un concept de classes "amies" comme le propose C++ permettrait
	 * d'éviter cette classe interne. Il n'existe pas de façon en Java d'émuler
	 * ce comportement.
	 */
	private class Delegate implements RadioServerEngineDelegate {
		/**
		 * Gestion de la connexion d'un nouveau client.
		 * <p>
		 * Lors d'une connexion, un nouveau {@link SocketManager} est créé pour
		 * gérer cette connexion.
		 */
		public void onNewConnection(RadioSocket socket) {
			// Memory management is weird here...
			//
			// This object launches 2 threads, these threads are inner-classes,
			// so they have an implicit reference to their parents. So this
			// object will not be garbage-collected by Java despite no direct
			// reference to it (at least until registered when ready)!
			new SocketManager(socket);
		}
	}

	// - - - Event Emitter - - -

	/**
	 * Gestionnaire de la file d'attente de messages entrants. Ce thread
	 * surveille la queue incomingMessages et notifie le délégué de la radio de
	 * l'arrivée de nouveaux messages.
	 */
	private class IncomingMessageEmitter extends Thread {
		/**
		 * Indicateur d'état. Le thread s'arrête si défini à <code>false</code>.
		 */
		private boolean running = true;

		public void run() {
			while(running) {
				Message message;
				try {
					message = incomingMessages.take(); // Blocking
					delegate.onMessage(message.getID(), message);
				}
				catch(InterruptedException e) {
					// -> incomingMessage.take() interrupted
					// nothing to do, loop.
				}
			}
		}

		/**
		 * Arrête le thread de notification.
		 */
		// TODO: cette méthode sera appelée à l'arrêt de la radio [NYI]
		public void quit() {
			running = false;
			this.interrupt();
		}
	}

	// - - - Socket Manager - - -

	/**
	 * Un gestionnaire de socket.
	 * <p>
	 * Cet objet est responsable de toute la gestion d'un client particulier.
	 * C'est à dire qu'il initialisera le thread d'écoute et d'écriture
	 * correspondant, surveillera l'état de la communication (HANDSHAKE, READY,
	 * ...) et s'occupera des messages protocolaires qui n'ont pas d'intérêt
	 * pour la tour elle-même (court-circuitage).
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
			listener.start();

			writer = new SocketWriter();
			writer.start();
		}

		/**
		 * Indique si le socket géré correspond à un client supportant le
		 * protocole étendu.
		 */
		public boolean isExtended() {
			return extended;
		}

		/**
		 * Upgrade les flux d'entrée/sortie en les passant en mode Extended
		 * plutôt que Legacy. Cette méthode execute simplement les méthodes
		 * <code>upgrade</code> des objets {@link SocketListener} et
		 * {@link SocketWriter} sous-jacents.
		 */
		private void upgrade() {
			try {
				listener.upgrade();
				writer.upgrade();
			}
			catch(IOException e) {
				// Failed to upgrade streams
				System.out.println("Failed to upgrade streams...");
				e.printStackTrace();
				quit();
			}
		}

		/**
		 * Passe ce SocketManager en état <code>READY</code>.
		 * <p>
		 * Il sera désormais possible de l'utiliser pour envoyer des messages
		 * quelconque et il sera inscrit dans la liste des gestionnaires actifs
		 * de la radio en utilisant le RadioID obtenu lors du handshake.
		 * <p>
		 * L'appel de cette méthode génère également une notification de type
		 * <code>onPlaneConnected</code> au délégué de la radio.
		 */
		private void ready() {
			synchronized(managers) {
				managers.put(socketID, this);
			}

			state = RadioSocketState.READY;
			delegate.onPlaneConnected(socketID);
		}

		/**
		 * Déconnecte de force le client associé à ce SocketManager. Cette
		 * méthode est appelée automatiquement par la méthode <code>kick</code>
		 * de la classe RadioServer.
		 * <p>
		 * Cette méthode appel ensuite quit() pour forcer l'arrêt et la
		 * déconnexion de ce client.
		 */
		public void kick() {
			// TODO: if extended, send KICK notice
			quit();
		}

		/**
		 * Arrête ce SocketManager et ferme le RadioSocket sous-jacent. Cette
		 * méthode provoque aussi l'arrêt des threads SocketListener et
		 * SocketWriter internes.
		 * <p>
		 * Cette méthode ne peut être appelée directement depuis l'extérieur.
		 * Elle est appelée implicitement par la méthode <code>kick</code> ou
		 * explicitement par les threads de lecture/écriture interne si une
		 * erreur provoquant la déconnexion du client survient lors de leur
		 * execution.
		 * <p>
		 * La bonne méthode pour déconnecter un client depuis l'extérieur est
		 * d'utiliser la méthode <code>kick</code>.
		 */
		private void quit() {
			synchronized(state) {
				// Prevent multiples calls
				// quit -> socket.close() -> IOException -> quit
				if(state == RadioSocketState.CLOSING)
					return;

				// Unregister
				if(state == RadioSocketState.READY) {
					// Disconnect notification
					// Notification must be the first thing done.
					delegate.onPlaneDisconnected(socketID);

					synchronized(managers) {
						managers.remove(socketID);
					}
				}

				state = RadioSocketState.CLOSING;
			}

			listener.quit();
			writer.quit();

			try {
				// TODO: if we must ensure empty output queue, socket closing
				// must be handled by Listener / Writer (and decomposed).
				socket.close();
			}
			catch(IOException e) {
				System.err.println("Error while closing socket!?");
				e.printStackTrace(System.err);
			}
		}

		// - - - Listener - - -

		/**
		 * Thread de gestion de l'entrée du socket. Ce thread utilise un flux
		 * d'entrée de messages pour recevoir les messages envoyés par le
		 * client, il effectue ensuite les traitements nécessaires si le message
		 * reçu doit être court-circuité (messages protocolaires) ou le place
		 * dans la liste d'attente des messages à transmettre à la tour.
		 */
		private class SocketListener extends Thread {
			/**
			 * Flux d'entrée de messages. Ce flux est de type
			 * UpgradableMessageInputStream pour pouvoir plus tard être
			 * dynamiquement modifié en flux de type étendu si cette version du
			 * protocole est supportée par le client.
			 */
			private UpgradableMessageInputStream mis;

			/**
			 * État du thread. Le thread s'arrête si défini à <code>false</code>
			 * .
			 */
			private boolean running = true;

			/**
			 * Gestionnaire des messages
			 */
			private RadioServerMessageHandler messageHandler;

			/**
			 * Crée un nouveau thread d'écoute d'entrée client.
			 */
			public SocketListener() {
				mis = new UpgradableMessageInputStream(socket.in);
				messageHandler = new RadioServerMessageHandler();
			}

			/**
			 * Méthode principale du thread.
			 */
			public void run() {
				try {
					Message message;

					while(running) {
						// Read one message from input stream.
						synchronized(mis) {
							message = mis.readMessage();
						}

						// Handle it!
						message.handle(messageHandler);
					}
				}
				catch(EOFException e) {
					// Connexion closed
					// TODO: EOF unexpected without BYE!
					SocketManager.this.quit();
				}
				catch(RadioProtocolException e) {
					// Invalid message for this state, disconnect plane.
					System.err.println("Protocol Exception from Plane");
					e.printStackTrace(System.err);
					SocketManager.this.quit();
				}
				catch(IOException e) {
					// If socket is closing, it's expected.
					if(state != RadioSocketState.CLOSING) {
						// Unable to read message, disconnect plane.
						System.err.println("Cannot read from plane socket");
						e.printStackTrace(System.err);
						SocketManager.this.quit();
					}
				}
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

			/**
			 * Gestionnaire des messages (Visitor Pattern)
			 */
			private class RadioServerMessageHandler implements MessageHandler {
				public void handle(MessageHello m) throws RadioProtocolException {
					// HELLO can be received only when in HANDSHAKE state
					if(state != RadioSocketState.HANDSHAKE) {
						throwInvalidState(m);
					}

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

						// TODO: upgrade only listener, writer will be upgraded later
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
				 * Gestion du message KeepAlive. Remet à zéro le timeout de
				 * déconnexion de l'avion en cas d'inactivité. [NYI]
				 */
				public void handle(MessageKeepalive m) {
					// Keepalive is used to reset socket timeout, handle it.
					// TODO: handle it

					// But is also used for updating plane position, so tower
					// must receive it.
					forwardToTower(m);
				}

				/**
				 * Gestion du message SendRSAKey. Lors de la réception de ce
				 * message, le flux de sortie est mis à jour afin de supporter
				 * le chiffrement avec la clé publique de l'avion. Le socket est
				 * ensuite défini à l'état <code>READY</code> et signalé comme
				 * nouvelle connexion à la tour.
				 */
				public void handle(MessageSendRSAKey m) throws RadioProtocolException {
					// Upgrade the output stream to write encrypted data with the
					// plane public key.
					RSAKeyPair planeKey = new RSAKeyPair(m.getKey());
					socket.out.upgrade(new RSAOutputStream(socket.out.getStream(), planeKey));

					// Socket is ready for general usage.
					SocketManager.this.ready();
				}

				// Unmanaged messages types

				public void handle(MessageBye m) throws RadioProtocolException {
					unmanagedMessage(m);
				}

				public void handle(MessageChoke m) throws RadioProtocolException {
					unmanagedMessage(m);
				}

				public void handle(MessageData m) throws RadioProtocolException {
					unmanagedMessage(m);
				}

				public void handle(MessageLanding m) throws RadioProtocolException {
					unmanagedMessage(m);
				}

				public void handle(MessageMayDay m) throws RadioProtocolException {
					unmanagedMessage(m);
				}

				public void handle(MessageRouting m) throws RadioProtocolException {
					unmanagedMessage(m);
				}

				public void handle(MessageUnchoke m) throws RadioProtocolException {
					unmanagedMessage(m);
				}

				// Helpers

				private void unmanagedMessage(Message m) throws RadioProtocolException {
					// If not in READY state, the message must be a
					// protocol message.
					// Since all protocol messages are short-circuited,
					// and we are in the default case, it's
					// obviously not a protocol message.
					if(state != RadioSocketState.READY) {
						throwInvalidState(m);
					}

					// No short circuit, send it to incoming queue
					forwardToTower(m);
				}

				/**
				 * Transmet le message à la tour de contrôle.
				 * 
				 * @param m
				 *            Le message à transmettre
				 */
				private void forwardToTower(Message m) {
					incomingMessages.put(m);
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
			}
		}

		// - - - Writer - - -

		/**
		 * Thread de gestion de la liste d'attente des messages sortants. Ce
		 * thread possède sa propre queue d'envoi de message depuis laquelle il
		 * récupérera les messages à envoyé à l'avion selon leur priorité.
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
			 * Crée un nouveau thread d'écriture vers un client. Ce thread
			 * utilisera le flux de sortie du socket du SocketManager.
			 */
			public SocketWriter() {
				mos = new UpgradableMessageOutputStream(socket.out);
			}

			public void run() {
				Message message;

				// TODO: ensure empty queue before quitting
				// flush all message including BYE before closing socket.
				while(running) {
					try {
						message = queue.take();
						synchronized(mos) {
							mos.writeMessage(message);
						}
					}
					catch(InterruptedException e) {
						// queue.take interrupted, loop.
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
			 * Envoie un message à l'avion. Le message est placé dans la file
			 * d'attente d'envoi et son envoi effectif sera différé. Si la file
			 * d'attente contient plusieurs messages, ceux de la priorité la
			 * plus élevée seront envoyés en premiers.
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
			 * Arrête le thread. Cet arrêt détruit tout les messages pas encore
			 * envoyés et qui sont dans la file d'attente. Néanmoins si un
			 * message est actuellement en cours d'envoi, son envoi sera terminé
			 * avant de terminer le thread.
			 */
			public void quit() {
				running = false;
				this.interrupt();
			}
		}
	}
}
