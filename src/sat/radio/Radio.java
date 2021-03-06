package sat.radio;

import java.io.EOFException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.PriorityBlockingQueue;

import sat.events.AsyncEventEmitter;
import sat.events.Event;
import sat.events.UnhandledEventException;
import sat.events.schedulers.PriorityEventScheduler;
import sat.radio.message.Message;
import sat.radio.message.MessageData;
import sat.radio.message.stream.MessageInputStream;
import sat.radio.message.stream.MessageOutputStream;
import sat.radio.socket.RadioSocket;
import sat.radio.socket.RadioSocketState;
import sat.utils.file.DataFile;
import sat.utils.geo.Coordinates;

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
	public Radio(RadioDelegate delegate, RadioID id) {
		super(new PriorityEventScheduler());

		this.delegate = delegate;
		this.id = id;
	}

	public boolean isCiphered() {
		return ciphered;
	}

	public void setCiphered(boolean ciphered) {
		this.ciphered = ciphered;
	}

	public boolean isLegacy() {
		return legacy;
	}

	public void setLegacy(boolean legacy) {
		this.legacy = legacy;
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

		protected boolean choked = false;

		public SocketManager(RadioSocket socket) {
			this.socket = socket;

			listener = new SocketListener();
			writer = new SocketWriter();
		}

		public void start() {
			listener.start();
			writer.start();
		}

		public void send(Message message) {
			if(choked && message.getPriority() > 3) {
				return;
			}
			writer.send(message);
		}

		public void sendFile(final DataFile file) {
			(new Thread() {
				public void run() {
					try {
						int i = 0;
						for(byte[] part : file) {
							Coordinates c = delegate.getLocation();
							send(new MessageData(id, c, file.getHash(), i, file.getFormat(), file.getSize(), part));
							i++;
							// TODO: sleep
						}
					}
					catch(NoSuchAlgorithmException e) {
						emitEvent(new RadioEvent.UncaughtException("Error hashing file", e));
					}
					catch(IOException e) {
						emitEvent(new RadioEvent.UncaughtException("Error when reading file", e));
					}
					finally {
						try {
							file.close();
						}
						catch(IOException e) {
						}
					}
				}
			}).start();
		}

		public void sendText(String text) {
			byte[] data = text.getBytes();

			if(data.length > 1024) {
				// TODO: what else?
				return;
			}

			byte[] hash;

			try {
				MessageDigest digest = MessageDigest.getInstance("SHA1");
				digest.update(data, 0, data.length);
				hash = digest.digest();
			}
			catch(NoSuchAlgorithmException e) {
				// TODO: what else?
				return;
			}

			Coordinates c = delegate.getLocation();
			send(new MessageData(id, c, hash, 0, "txt", data.length, data));
		}

		public void setChoked(boolean choked) {
			this.choked = choked;
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
			private MessageInputStream mis;

			/**
			 * État du thread.
			 */
			private boolean running = true;

			public SocketListener() {
				mis = new MessageInputStream(socket.in);
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

			public void upgrade() {
				mis.setExtended(true);
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
			private MessageOutputStream mos;

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
				mos = new MessageOutputStream(socket.out);
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

			public void upgrade() {
				mos.setExtended(true);
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
