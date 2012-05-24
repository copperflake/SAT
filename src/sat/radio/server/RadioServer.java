package sat.radio.server;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import sat.events.Event;
import sat.events.EventListener;
import sat.events.UnhandledEventException;
import sat.radio.Radio;
import sat.radio.RadioEvent;
import sat.radio.RadioID;
import sat.radio.RadioProtocolException;
import sat.radio.engine.server.RadioServerEngine;
import sat.radio.engine.server.RadioServerEngineDelegate;
import sat.radio.message.*;
import sat.radio.socket.RadioSocket;
import sat.radio.socket.RadioSocketState;
import sat.utils.crypto.RSAInputStream;
import sat.utils.crypto.RSAKey;
import sat.utils.crypto.RSAKeyPair;
import sat.utils.crypto.RSAOutputStream;
import sat.utils.file.DataFile;
import sat.utils.geo.Coordinates;
import sat.utils.routes.RoutingType;
import sat.utils.routes.Waypoint;

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
	 * Liste des pairs connectés avec le gestionnaire associé. Chaque Manager a
	 * la responsabilité de s'ajouter dans cette liste lorsqu'il devient prêt à
	 * être utilisé.
	 */
	private HashMap<RadioID, PlaneAgent> agents;

	/**
	 * Crée un nouveau serveur radio qui dépend du délégué spécifié.
	 * 
	 * @param delegate
	 *            Le délégué qui sera chargé de la gestion des événements de la
	 *            radio.
	 */
	public RadioServer(RadioServerDelegate delegate, RadioID id) {
		super(delegate, id);
		this.delegate = delegate; // TODO: useful ?
		this.agents = new HashMap<RadioID, PlaneAgent>();
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
		synchronized(agents) {
			if(agents.containsKey(id)) {
				agents.get(id).kick();
			}
		}
	}

	public PlaneAgent getAgentForId(RadioID id) {
		synchronized(agents) {
			return agents.get(id);
		}
	}

	public void send(RadioID id, Message m) {
		PlaneAgent agent = getAgentForId(id);
		if(agent != null) {
			agent.send(m);
		}
	}

	public void broadcast(Message m) {
		synchronized(agents) {
			for(PlaneAgent plane : agents.values()) {
				plane.send(m);
			}
		}
	}

	public void sendFile(RadioID id, DataFile file) {
		PlaneAgent agent = getAgentForId(id);
		if(agent != null) {
			agent.sendFile(file);
		}
		else {
			try {
				file.close();
			}
			catch(IOException e) {
			}
		}
	}

	public void sendText(RadioID id, String text) {
		PlaneAgent agent = getAgentForId(id);
		if(agent != null) {
			agent.sendText(text);
		}
	}

	public void sendRouting(RadioID id, Waypoint waypoint, RoutingType routingType) {
		send(id, new MessageRouting(id, waypoint, routingType));
	}

	public void sendChoke() {
		broadcast(new MessageChoke(id, delegate.getLocation()));
	}

	public void sendUnchoke() {
		broadcast(new MessageUnchoke(id, delegate.getLocation()));
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
		 * Lors d'une connexion, un nouveau {@link PlaneAgent} est créé pour
		 * gérer cette connexion.
		 */
		public void onNewConnection(RadioSocket socket) {
			// Memory management is weird here...
			//
			// This object launches 2 threads, these threads are inner-classes,
			// so they have an implicit reference to their parents. So this
			// object will not be garbage-collected by Java despite no direct
			// reference to it (at least until registered when ready)!
			new PlaneAgent(socket);
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
	public class PlaneAgent extends SocketManager {
		/**
		 * L'id de l'avion auquel correspond ce socket.
		 */
		private RadioID socketID;

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
		 * Gestionnaire de messages
		 */
		private MessageHandler messageHandler;

		/**
		 * Crée un gestionnaire de socket.
		 * 
		 * @param socket
		 *            Le socket à gérer.
		 */
		public PlaneAgent(RadioSocket socket) {
			super(socket);

			messageHandler = new MessageHandler();

			start();
		}

		/**
		 * Indique si le socket géré correspond à un client supportant le
		 * protocole étendu.
		 */
		public boolean isExtended() {
			return extended;
		}

		protected void ready() {
			super.ready();

			synchronized(agents) {
				agents.put(socketID, this);
			}

			RadioServer.this.emit(new RadioEvent.PlaneConnected(socketID));
		}

		protected void quit() {
			synchronized(state) {
				// Prevent multiples calls
				// quit -> socket.close() -> IOException -> quit
				if(state == RadioSocketState.CLOSING) {
					return;
				}

				// Unregister
				if(state == RadioSocketState.READY) {
					// Disconnect notification
					// Notification must be the first thing done.
					RadioServer.this.emit(new RadioEvent.PlaneDisconnected(socketID));

					synchronized(agents) {
						agents.remove(socketID);
					}
				}

				// Call super-fail
				super.quit();
			}
		}

		protected void handleMessage(Message message) throws UnhandledEventException, InvocationTargetException {
			emit(new RadioEvent.MessageReceived(message));
			message.trigger(messageHandler);
		}

		protected void emitEvent(Event event) {
			RadioServer.this.emit(event);
		}

		/**
		 * Renvoi le message envoyé aux listeners de la tour, pour les journaux.
		 */
		public void send(Message message) {
			super.send(message);
			emit(new RadioEvent.MessageSent(message));
		}

		/**
		 * Gestionnaire des messages
		 */
		public class MessageHandler implements EventListener {
			public void on(MessageHello m) {
				// HELLO can be received only when in HANDSHAKE state
				if(state != RadioSocketState.HANDSHAKE) {
					invalidState(m);
				}

				// Enable extended protocol if not disabled
				if(!RadioServer.this.legacy) {
					extended = m.isExtended();
				}

				// Enable encryption
				if(RadioServer.this.ciphered) {
					ciphered = m.isCiphered();
				}

				Coordinates coords = delegate.getLocation();
				writer.send(new MessageHello(id, coords, ciphered, extended));

				socketID = m.getID();

				if(extended) {
					state = RadioSocketState.EXTENDED_HANDSHAKE;
					listener.upgrade();
					// Then wait for Upgrade message...
				}
				else if(ciphered) {
					state = RadioSocketState.CIPHER_NEGOCIATION;
					socket.in.upgrade(new RSAInputStream(socket.in.getStream(), delegate.getKeyPair()));
				}
				else {
					// Socket is ready!
					ready();
				}
			}

			public void on(MessageUpgrade m) {
				// HELLO can be received only when in EXTENDED_HANDSHAKE state
				if(state != RadioSocketState.EXTENDED_HANDSHAKE) {
					invalidState(m);
				}

				socketID = m.getID();
				writer.upgrade();

				if(ciphered) {
					Coordinates coords = delegate.getLocation();
					RSAKey key = delegate.getKeyPair().getPublicKey();
					writer.send(new MessageSendRSAKey(id, coords, key));

					socket.in.upgrade(new RSAInputStream(socket.in.getStream(), delegate.getKeyPair()));
					state = RadioSocketState.CIPHER_NEGOCIATION;
				}
				else {
					// Socket is ready!
					ready();
				}
			}

			/**
			 * Gestion du message KeepAlive. Remet à zéro le timeout de
			 * déconnexion de l'avion en cas d'inactivité. [NYI]
			 */
			public void on(MessageKeepalive m) {
				// Keepalive is used to reset socket timeout, handle it.
				// TODO: handle it

				// But is also used for updating plane position, so tower
				// must receive it.
				RadioServer.this.emit(m);
			}

			/**
			 * Gestion du message SendRSAKey. Lors de la réception de ce
			 * message, le flux de sortie est mis à jour afin de supporter le
			 * chiffrement avec la clé publique de l'avion. Le socket est
			 * ensuite défini à l'état <code>READY</code> et signalé comme
			 * nouvelle connexion à la tour.
			 */
			public void on(MessageSendRSAKey m) {
				// Upgrade the output stream to write encrypted data with the
				// plane public key.
				RSAKeyPair planeKey = new RSAKeyPair(m.getKey());
				socket.out.upgrade(new RSAOutputStream(socket.out.getStream(), planeKey));

				// Socket is ready for general usage.
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

				forwardToTower(m);
			}

			// Helpers

			/**
			 * Transmet le message à la tour de contrôle.
			 * 
			 * @param m
			 *            Le message à transmettre
			 */
			private void forwardToTower(Message m) {
				RadioServer.this.emit(m);
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
			private void invalidState(Message m) {
				// Invalid message for this state, disconnect plane.
				System.err.println("Protocol Exception from Plane");
				System.err.println("Cannot receive " + m.getType() + " in state " + state);
				quit();
			}
		}
	}
}
