package sat.radio.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.concurrent.PriorityBlockingQueue;

import sat.radio.Radio;
import sat.radio.RadioID;
import sat.radio.engine.server.RadioServerEngine;
import sat.radio.engine.server.RadioServerEngineDelegate;
import sat.radio.message.Message;
import sat.radio.message.MessageHello;
import sat.radio.message.stream.UpgradableMessageInputStream;
import sat.radio.message.stream.UpgradableMessageOutputStream;
import sat.radio.socket.RadioSocket;
import sat.utils.geo.Coordinates;

/**
 * Un serveur radio.
 * <p>
 * Un serveur radio écoute un flux d'entrée en attente de client radio. Il
 * utilise en interne un moteur de serveur radio qui s'occupe de gérer la partie
 * technique des communications.
 */
public class RadioServer extends Radio implements RadioServerEngineDelegate {
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
	private Thread incomingThread;

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
		super(label);

		setDelegate(delegate);

		this.managers = new HashMap<RadioID, SocketManager>();

		incomingMessages = new PriorityBlockingQueue<Message>();

		incomingThread = new IncomingMessageEmitter();
		incomingThread.start();
	}

	/**
	 * Modifie le délégué du serveur radio.
	 * 
	 * @param delegate
	 *            Nouveau délégué.
	 */
	public void setDelegate(RadioServerDelegate delegate) {
		this.delegate = delegate;
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
		this.engine.init(this);
	}

	// - - - Engine Events - - -

	/**
	 * Gestion de la connexion d'un nouveau client.
	 */
	public void onNewConnection(RadioSocket socket) {
		new SocketManager(socket).start();
	}

	// - - - Threads Objects - - -

	private class IncomingMessageEmitter extends Thread {
		private boolean running = true;

		public void run() {
			while(running) {
				Message message;
				try {
					message = incomingMessages.take();
					System.out.println("Got message " + message);
					// TODO: emit the message !
				} catch(InterruptedException e) {
				}
			}
		}

		public void quit() {
			running = false;
		}
	}

	/**
	 * Un gestionnaire de socket.
	 */
	private class SocketManager {
		/**
		 * Le socket géré par ce gestionnaire.
		 */
		private RadioSocket socket;

		/**
		 * Le thread d'écoute d'entrée.
		 */
		private SocketListener listener;

		/**
		 * Le thread d'écoute de sortie.
		 */
		private SocketWriter writer;

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

		public void quit() {
			try {
				socket.close();
			} catch(IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
					// TODO: handle null
					while((message = mis.readMessage()) != null && running) {
						handleMessage(message);
					}
				} catch(Exception e) {
					// Close bad client
					e.printStackTrace();
					SocketManager.this.quit();
				}
			}

			/**
			 * Gestion du court-circuitage des messages protocolaires.
			 */
			private void handleMessage(Message m) {
				switch(m.getType()) {
					case HELLO:
						handleMessage((MessageHello) m);
						break;

					default:
						// No short circuit, send it to incoming queue
						incomingMessages.put(m);
				}
			}

			/**
			 * Gestion du message Hello.
			 */
			private void handleMessage(MessageHello m) {
				// Enable encryption
				ciphered = m.isCiphered();

				// Enable extended protocol
				extended = m.isExtended();

				Coordinates coords = delegate.getLocation();
				writer.send(new MessageHello(id, coords, ciphered, extended));
				
				if(extended) {
					try {
						upgrade();
						writer.upgrade();
					} catch(IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			
			public void upgrade() throws IOException {
				mis.upgrade();
			}

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
						System.out.print("Sending " + message);
						mos.writeMessage(message);
					} catch(InterruptedException e) {
					} catch(IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

			public void send(Message m) {
				queue.put(m);
			}
			
			public void upgrade() throws IOException {
				mos.upgrade();
			}

			public void quit() {
				running = false;
				this.interrupt();
			}
		}
	}
}
