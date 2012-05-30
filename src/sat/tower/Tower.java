package sat.tower;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.TreeSet;

import sat.DebugEvent;
import sat.events.AsyncEventEmitter;
import sat.events.Event;
import sat.events.EventListener;

import sat.plane.PlaneType;
import sat.radio.RadioDelegate;
import sat.radio.RadioEvent;

import sat.radio.RadioID;
import sat.radio.engine.server.RadioServerEngine;
import sat.radio.message.*;
import sat.radio.server.RadioServer;
import sat.utils.cli.Config;
import sat.utils.crypto.RSAException;
import sat.utils.crypto.RSAKeyPair;
import sat.utils.geo.Coordinates;
import sat.utils.geo.InvalidCoordinatesException;
import sat.utils.pftp.FileTransferAgentDispatcher;
import sat.utils.pftp.FileTransferDelegate;
import sat.utils.routes.*;

/**
 * Une tour de contrôle. Cette classe est un Singleton.
 */
public class Tower extends AsyncEventEmitter implements EventListener, RadioDelegate {
	// - - - Singleton Tools - - -

	/**
	 * L'instance unique de la classe Tower.
	 */
	private static Tower instance;

	/**
	 * La configuration par défaut d'une tour. Sert de modèle à la contruction
	 * de la configuration spécifique aux instances d'une tour.
	 */
	private static Config defaults;

	/**
	 * Initialise la configuration par défaut avec les valeurs appropriées.
	 */
	private static void initDefaults() {
		defaults = new Config();

		defaults.setProperty("tower.debug", "no");
		defaults.setProperty("tower.prefix", "TWR");
		defaults.setProperty("tower.downloads", "downloads/");
		defaults.setProperty("tower.routing", "chronos");
		defaults.setProperty("tower.graveyard", "600,100,-1");

		defaults.setProperty("radio.ciphered", "yes");
		defaults.setProperty("radio.legacy", "no");
		defaults.setProperty("radio.keylength", "1024");
	}

	/**
	 * Constructeur privé. Impossible de créer une instance de cette classe
	 * directement. Utiliser <code>Tower.getInstance()</code> pour obtenir
	 * l'instance-unique de la tour.
	 */
	private Tower() {
		config = new Config(defaults);

		dataDispatcher = new FileTransferAgentDispatcher(new FileTransferDelegate() {
			public void planeIdentified(RadioID id, PlaneType type) {
				emit(new TowerEvent.PlaneIdentified(id, type));
			}

			public String getDownloadsPath() {
				return config.getString("tower.downloads");
			}

			public void debugEvent(DebugEvent ev) {
				emitDebug(ev);
			}

			public void transferComplete(String path) {
				emit(new TowerEvent.TransferComplete(path));
			}
		});
	}

	/**
	 * Retourne l'unique instance de la classe Tower. Si cette instance n'existe
	 * pas encore, elle est automatiquement créée.
	 * 
	 * @return L'instance unique de la classe Tower.
	 */
	public static Tower getInstance() {
		if(instance == null) {
			initDefaults();
			instance = new Tower();
		}

		return instance;
	}

	// - - - Class methods - - -

	/**
	 * Le serveur-radio de la tour. Le serveur radio est chargé de toute la
	 * gestion technique de la communication avec le monde extérieur.
	 */
	private RadioServer radio = null;

	/**
	 * La configuration spécifique à une instance de la tour (même si en
	 * pratique, la tour est un singleton).
	 */
	private Config config;

	/**
	 * La clé de cette tour de contrôle.
	 */
	private RSAKeyPair keyPair;

	/**
	 * L'identifiant de la tour.
	 */
	private RadioID id;

	/**
	 * Indique si cet tour a été initialisée.
	 */
	private boolean initDone = false;

	/**
	 * Le gestionnaire de dispatch des messages de données.
	 */
	private FileTransferAgentDispatcher dataDispatcher;

	/**
	 * Avions connectés à cette tour.
	 */
	private HashMap<RadioID, TowerPlane> planes = new HashMap<RadioID, TowerPlane>();

	/**
	 * Liste des routes (circuits d'attente et piste d'atterissage)
	 */
	private ArrayList<Route> routes = new ArrayList<Route>();

	/**
	 * Charge un fichier de route, le lit, le parse et ajoute une route à
	 * <code>routes</code>.
	 * 
	 * @param path
	 *            Le fichier contenant la route.
	 * @param capacity
	 *            Le nombre d'avion maximum sur la route.
	 * @throws Exception
	 */
	public void loadRoute(String path, int capacity) throws Exception {
		StringBuffer fileData = new StringBuffer();
		BufferedReader reader = new BufferedReader(new FileReader(path));

		char[] buf = new char[1024];
		int numRead = 0;

		while((numRead = reader.read(buf)) > 0) {
			String readData = String.valueOf(buf, 0, numRead);
			fileData.append(readData);
		}

		reader.close();
		String data = fileData.toString();

		Route route = new Route(capacity);

		for(String instruction : data.split(";")) {
			if(instruction.matches("\\s*")) {
				continue;
			}

			char t = instruction.toUpperCase().charAt(0);
			String[] coords = instruction.substring(1).split(",");
			float[] floatCoords = new float[coords.length];

			for(int i = 0; i < coords.length; i++) {
				floatCoords[i] = Float.parseFloat(coords[i]);
			}

			MoveType type;
			float[] args;

			switch(t) {
				case 'S':
					type = MoveType.STRAIGHT;
					args = new float[] { floatCoords[0], floatCoords[1], -1 };
					break;

				case 'C':
					type = MoveType.CIRCULAR;
					args = new float[] { floatCoords[0], floatCoords[1], -1, floatCoords[2] };
					break;

				case 'L':
					type = MoveType.LANDING;
					args = new float[] { floatCoords[0], floatCoords[1], -1 };
					route.setLanding();
					break;

				case 'N':
					type = MoveType.NONE;
					args = new float[] {};
					break;

				case 'D':
					type = MoveType.DESTRUCTION;
					args = new float[] { floatCoords[0], floatCoords[1], -1 };
					break;

				default:
					continue;
			}

			route.add(new Waypoint(type, args));
		}

		if(route.isLanding()) {
			for(Route otherRoute : routes) {
				if(!otherRoute.isLanding()) {
					throw new Exception("Cannot load a landing route when a not-landing route is already loaded.");
				}
			}
		}

		routes.add(route);
	}

	/**
	 * Retourne l'objet de configuration de la tour.
	 */
	public Config getConfig() {
		return config;
	}

	public void choke() {
		radio.sendChoke();
	}

	public void unchoke() {
		radio.sendUnchoke();
	}

	/**
	 * Initialise la tour de contrôle en fonction de paramètre de configuration
	 * actifs à ce moment.
	 */
	public void init() {
		if(initDone) {
			return;
		}

		// ID
		id = new RadioID(config.getString("tower.prefix"));

		// Radio
		radio = new RadioServer(this, id);
		radio.addListener(this);

		radio.setCiphered(config.getBoolean("radio.ciphered"));
		radio.setLegacy(config.getBoolean("radio.legacy"));

		initDone = true;
	}

	/**
	 * Ajoute un moteur de radio à la radio de la tour et l'initialise.
	 * 
	 * @param engine
	 *            Le moteur de radio à ajouter.
	 * 
	 * @throws IOException
	 *             L'initilisation du moteur peut provoquer une
	 *             <code>IOException</code> qui est passée au code appelant.
	 */
	public void listen(RadioServerEngine engine) throws IOException {
		radio.listen(engine);
	}

	/**
	 * Actualise les routes des avions et les notifie des éventuels changement.
	 */
	public void refreshRouting() {
		String routingModeRaw = config.getString("tower.routing").toLowerCase();

		emitDebug("[ROUTING] Refreshing routes");

		final RoutingMode routingMode;

		if(routingModeRaw.equals("fuel")) {
			routingMode = RoutingMode.FUEL;
		}
		else if(routingModeRaw.equals("time")) {
			routingMode = RoutingMode.TIME;
		}
		else {
			routingMode = RoutingMode.CHRONOS;
		}

		emitDebug("[ROUTING] Route mode is " + routingMode);

		TreeSet<TowerPlane> planes = new TreeSet<TowerPlane>(new Comparator<TowerPlane>() {
			public int compare(TowerPlane p1, TowerPlane p2) {
				if(p1.isLanding() != p2.isLanding()) {
					return (p1.isLanding()) ? -1 : 1;
				}
				else if(p1.isMayDay() != p2.isMayDay()) {
					return (p1.isMayDay()) ? -1 : 1;
				}
				else {
					RoutingMode localRoutingMode = routingMode;

					if(p1.getType() == null && p2.getType() == null) {
						localRoutingMode = RoutingMode.CHRONOS;
					}
					else if(p1.getType() == null || p2.getType() == null) {
						return (p1.getType() == null) ? 1 : -1;
					}
					else if(p1.isMayDay()) { // and p2.isMayDay()
						if(p1.getType().passengers == p2.getType().passengers) {
							localRoutingMode = RoutingMode.CHRONOS; // Saving passengers
						}
						else {
							localRoutingMode = RoutingMode.TIME; // Saving passengers
						}
					}

					switch(localRoutingMode) {
						case FUEL:
							return (p1.getType().consumption > p2.getType().consumption) ? -1 : 1;

						case TIME:
							return (p1.getType().passengers > p2.getType().passengers) ? -1 : 1;

						default: // CHRONOS
							return (p1.getLandingID() < p2.getLandingID()) ? -1 : 1;
					}
				}
			}
		});

		// Add planes to waiting list
		for(TowerPlane plane : this.planes.values()) {
			// We ignore plane without landing requested
			if(plane.getLandingID() != -1) {
				planes.add(plane);
			}
		}

		emitDebug("[ROUTING] Step 1/2, done");

		int currentRoute = 0;
		int planesAssigned = 0;

		// Assign routes
		for(TowerPlane plane : planes) {
			if(currentRoute >= routes.size()) {
				currentRoute = -1;
			}

			if(plane.getCurrentRoute() != currentRoute) {
				if(currentRoute < 0) {
					Route highwayToHell = new Route();

					try {
						highwayToHell.add(new Waypoint(MoveType.DESTRUCTION, Coordinates.parseCoordinates(config.getProperty("tower.graveyard")).toFloats()));
					}
					catch(InvalidCoordinatesException e) {
						radio.kick(plane.getID());
						continue;
					}

					defineRoute(plane, highwayToHell, true);
				}
				else {
					try {
						defineRoute(plane, routes.get(currentRoute), true);
					}
					catch(Exception e) {
						e.printStackTrace();
					}
				}

				plane.setCurrentRoute(currentRoute);
			}

			planesAssigned++;

			if(currentRoute != -1 && planesAssigned >= routes.get(currentRoute).getCapacity()) {
				currentRoute++;
				planesAssigned = 0;
			}
		}

		emitDebug("[ROUTING] Step 2/2, done");
	}

	/**
	 * Défini la route d'un avion en lui envoyant les instructions de routage
	 * appropriées.
	 */
	private void defineRoute(TowerPlane plane, Route route, boolean replace) {
		RadioID id = plane.getID();

		emitDebug("[ROUTING] Redefining route for " + id);

		plane.setLoopPoint(route.getLoopPoint());

		if(route.isLanding()) {
			plane.setLanding();
		}

		route = (Route) route.clone();

		if(replace) {
			radio.sendRouting(id, route.remove(0), RoutingType.REPLACEALL);
		}

		for(Waypoint waypoint : route) {
			radio.sendRouting(id, waypoint, RoutingType.LAST);
		}
	}

	// - - - Radio Delegate - - -

	/**
	 * Retourne les coordonées de la tour.
	 */
	public Coordinates getLocation() {
		return new Coordinates(0, 0, 0);
	}

	/**
	 * Retourne la clé de la tour.
	 */
	public RSAKeyPair getKeyPair() {
		if(keyPair == null) {
			try {
				keyPair = new RSAKeyPair(config.getInt("radio.keylength"));
			}
			catch(RSAException e) {
				// Invalid key length, ignore given length and use default
				keyPair = new RSAKeyPair();
			}
		}

		return keyPair;
	}

	// - - - Radio Events - - -

	public void on(MessageKeepalive m) {
		emit(new TowerEvent.PlaneMoved(m.getID(), m.getCoordinates()));

		TowerPlane plane = planes.get(m.getID());

		float d = plane.distanceToLoopPoint(m.getCoordinates());
		if(!Float.isNaN(d) && d < 10) {
			if(!plane.isLoopSent()) {
				defineRoute(plane, routes.get(plane.getCurrentRoute()), false);
				plane.setLoopSent(true);
			}
		}
		else {
			plane.setLoopSent(false);
		}

		emit(m);
	}

	public void on(MessageData m) {
		// TODO: something asynchronous?
		dataDispatcher.dispatchMessageToAgent(m);
	}

	public synchronized void on(MessageLanding m) {
		planes.get(m.getID()).landingRequested();
		refreshRouting();
	}

	public void on(MessageBye m) {
		radio.kick(m.getID());
	}

	public synchronized void on(MessageMayDay m) {
		planes.get(m.getID()).setMayDay(true);
		emit(new TowerEvent.PlaneDistress(m.getID()));
		refreshRouting();
	}

	public void on(Message m) {
		emitDebug(m.toString());
	}

	public void on(RadioEvent.PlaneConnected e) {
		planes.put(e.getID(), new TowerPlane(e.getID()));
		emit(e);
	}

	public synchronized void on(RadioEvent.PlaneDisconnected e) {
		planes.remove(e.getID());
		refreshRouting();
		emit(e); // reemit
	}

	public void on(Event e) {
		// Unmanaged event, pass it to our own listeners
		emit(e);
	}

	/**
	 * Emet un message de debug contenant le message donné.
	 */
	private void emitDebug(String msg) {
		emitDebug(new DebugEvent(msg));
	}

	/**
	 * Emet un message de debug.
	 */
	private void emitDebug(DebugEvent event) {
		if(config.getBoolean("tower.debug")) {
			emit(event);
		}
	}
}
