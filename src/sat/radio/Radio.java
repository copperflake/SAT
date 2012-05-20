package sat.radio;

import java.io.EOFException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.PriorityBlockingQueue;

import sat.events.AsyncEventEmitter;
import sat.events.Event;
import sat.events.UnhandledEventException;
import sat.events.schedulers.PriorityEventScheduler;
import sat.radio.message.Message;
import sat.radio.message.stream.UpgradableMessageInputStream;
import sat.radio.message.stream.UpgradableMessageOutputStream;
import sat.radio.socket.RadioSocket;
import sat.radio.socket.RadioSocketState;
import sat.utils.crypto.RSAKeyPair;

/**
 * Une radio non-spécialisée (ni client, ni serveur). Cette classe fourni les
 * outils communs utilisés à la fois par les serveurs-radio et les
 * client-radios.
 */
public abstract class Radio extends AsyncEventEmitter {
	/**
	 * L'identifiant de cette radio. Tous les éléments d'un réseau radio SAT
	 * possède un identifiant unique l'identifiant sur le réseau.
	 */
	protected RadioID id;

	/**
	 * Le délégué de cette radio.
	 */
	protected RadioDelegate delegate;

	/**
	 * Défini le niveau de verbosité de la radio
	 */
	protected boolean verbose = false;

	/**
	 * Défini si la radio doit utiliser le cryptage
	 */
	protected boolean ciphered = true;

	/**
	 * Défini la radio ne doit pas utiliser le protocole étendu
	 */
	protected boolean legacy = false;

	/**
	 * Crée une nouvelle radio avec un label d'identifiant et une longueur de
	 * clé définie.
	 * 
	 * @param label
	 *            Le label de l'identifiant de cette radio.
	 * @param keyLength
	 *            La longueur de clé à utiliser pour le chiffrement.
	 */
	public Radio(RadioDelegate delegate) {
		super(new PriorityEventScheduler());

		this.delegate = delegate;
		id = delegate.getRadioID();
	}

	/**
	 * Retourne la clé utilisée par cette radio tel que fournie par le délégué.
	 */
	public RSAKeyPair getKeyPair() {
		return delegate.getKeyPair();
	}

	// - - - Socket Manager - - -

	protected abstract class SocketManager {
		/**
		 * Le socket géré par ce gestionnaire.
		 */
		protected RadioSocket socket;

		/**
		 * Le thread d'écoute d'entrée.
		 */
		protected SocketListener listener;

		/**
		 * Le thread d'écoute de sortie.
		 */
		protected SocketWriter writer;

		/**
		 * État de cette connexion.
		 */
		protected RadioSocketState state = RadioSocketState.HANDSHAKE;

		public SocketManager(RadioSocket socket) {
			this.socket = socket;

			listener = new SocketListener();
			writer = new SocketWriter();

			listener.start();
			writer.start();
		}

		public void send(Message message) {
			writer.send(message);
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
		protected void ready() {
			state = RadioSocketState.READY;
		}

		/**
		 * Upgrade les flux d'entrée/sortie en les passant en mode Extended
		 * plutôt que Legacy. Cette méthode execute simplement les méthodes
		 * <code>upgrade</code> des objets {@link SocketListener} et
		 * {@link SocketWriter} sous-jacents.
		 */
		protected void upgrade() {
			try {
				listener.upgrade();
				writer.upgrade();
			}
			catch(IOException e) {
				// Failed to upgrade streams
				emitEvent(new RadioEvent.UncaughtException("Failed to upgrade streams...", e));
				quit();
			}
		}

		/**
		 * Déconnecte de force le client associé à ce SocketManager.
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
		 */
		protected void quit() {
			synchronized(state) {
				// Prevent multiples calls
				// quit -> socket.close() -> IOException -> quit
				if(state == RadioSocketState.CLOSING) {
					return;
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
				emitEvent(new RadioEvent.UncaughtException("Error while closing socket ?", e));
			}
		}

		protected abstract void handleMessage(Message message) throws InvocationTargetException, UnhandledEventException;
		protected abstract void emitEvent(Event event);

		// - - - Listener - - -

		protected class SocketListener extends Thread {
			/**
			 * Flux d'entrée de messages. Ce flux est de type
			 * UpgradableMessageInputStream pour pouvoir plus tard être
			 * dynamiquement modifié en flux de type étendu si cette version du
			 * protocole est supportée par le client.
			 */
			private UpgradableMessageInputStream mis;

			/**
			 * État du thread.
			 */
			private boolean running = true;

			public SocketListener() {
				mis = new UpgradableMessageInputStream(socket.in);
			}

			public void run() {
				try {
					Message message;

					while(running) {
						// Read one message from input stream.
						synchronized(mis) {
							message = mis.readMessage();
						}

						// Handle it!
						handleMessage(message);
					}
				}
				catch(EOFException e) {
					// Connexion closed
					// TODO: EOF unexpected without BYE!
					SocketManager.this.quit();
				}
				catch(IOException e) {
					// If socket is closing, it's expected.
					if(state != RadioSocketState.CLOSING) {
						// Unable to read message, disconnect plane.
						emitEvent(new RadioEvent.UncaughtException("Cannot read from plane socket", e));
						SocketManager.this.quit();
					}
				}
				catch(UnhandledEventException e) {
					emitEvent(new RadioEvent.UncaughtException("Error handling message", e));
					SocketManager.this.quit();
				}
				catch(InvocationTargetException e) {
					emitEvent(new RadioEvent.UncaughtException("Exception when handling message", e.getTargetException()));
					SocketManager.this.quit();
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
		}

		protected class SocketWriter extends Thread {
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
						emitEvent(new RadioEvent.UncaughtException("Cannot write to plane socket", e));

						// Unable to write message, disconnect plane.
						SocketManager.this.quit();
					}
				}
			}

			/**
			 * Envoie un message. Le message est placé dans la file d'attente
			 * d'envoi et son envoi effectif sera différé. Si la file d'attente
			 * contient plusieurs messages, ceux de la priorité la plus élevée
			 * seront envoyés en premiers.
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
				// TODO: Ensure the message output stream is not in use
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
