package sat.radio;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.PriorityQueue;

import sat.radio.engine.server.RadioServerEngine;
import sat.radio.engine.server.RadioServerEngineDelegate;
import sat.radio.message.Message;

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
	private PriorityQueue<Message> incomingMessages;

	/**
	 * File d'attente des messages sortants.
	 */
	private PriorityQueue<Message> outgoingMessages;

	/**
	 * Thread de gestion des messages entrants.
	 */
	private Thread incomingThread;

	/**
	 * Thread de gestion des messages sortants.
	 */
	private Thread outgoingThread;

	/**
	 * Liste des pairs connectés avec le socket radio associé.
	 */
	private HashMap<RadioID, RadioSocket> sockets;

	/**
	 * Crée un nouveau serveur radio.
	 * 
	 * @param delegate
	 *            Le délégué qui sera chargé de la gestion des événements de la
	 *            radio.
	 */
	public RadioServer(RadioServerDelegate delegate) {
		setDelegate(delegate);

		this.sockets = new HashMap<RadioID, RadioSocket>();

		incomingMessages = new PriorityQueue<Message>();
		outgoingMessages = new PriorityQueue<Message>();

		incomingThread = new Thread() {
			public void run() {
				synchronized(incomingMessages) {
					while(true) {
						if(!incomingMessages.isEmpty()) {
							Message message = incomingMessages.poll();
							System.out.println("Got message " + message);
						} else {
							try {
								incomingMessages.wait();
							} catch(InterruptedException e) {
								// TODO handle
								e.printStackTrace();
							}
						}
					}
				}
			}
		};

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
		new RadioSocketListener(socket).start();
	}

	/**
	 * Thread de gestion de l'entrée client.
	 */
	protected class RadioSocketListener extends Thread {
		/**
		 * Le socket géré par ce thread
		 */
		RadioSocket socket;

		/**
		 * Crée un nouveau thread d'écoute d'entrée client sur un socket
		 * spécifique.
		 * 
		 * @param socket
		 *            Le socket à écouter.
		 */
		public RadioSocketListener(RadioSocket socket) {
			this.socket = socket;
		}

		/**
		 * Méthode principale du thread.
		 */
		public void run() {
			try {
				ObjectInputStream ois = new ObjectInputStream(socket.in);
				System.out.println("Client Connected");

				Message message;
				while((message = (Message) ois.readObject()) != null) {
					synchronized(incomingMessages) {
						incomingMessages.add(message);
						incomingMessages.notify();
					}
				}
			} catch(Exception e) {
				// Close bad client
				e.printStackTrace();
				try {
					socket.close();
				} catch(IOException e1) {
					// TODO handle
					e1.printStackTrace();
				}
			}
		}
	}
}
