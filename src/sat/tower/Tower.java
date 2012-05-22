package sat.tower;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import sat.DebugEvent;
import sat.events.AsyncEventEmitter;
import sat.events.Event;
import sat.events.EventListener;

import sat.radio.RadioEvent;

import sat.radio.RadioID;
import sat.radio.engine.server.RadioServerEngine;
import sat.radio.message.Message;
import sat.radio.message.MessageData;
import sat.radio.message.MessageKeepalive;
import sat.radio.server.RadioServer;
import sat.radio.server.RadioServerDelegate;
import sat.utils.cli.Config;
import sat.utils.crypto.RSAException;
import sat.utils.crypto.RSAKeyPair;
import sat.utils.file.DataFile;
import sat.utils.geo.Coordinates;
import sat.utils.routes.MoveType;
import sat.utils.routes.Route;
import sat.utils.routes.Waypoint;

/**
 * Une tour de contrôle. Cette classe est un Singleton.
 */
public class Tower extends AsyncEventEmitter implements EventListener, RadioServerDelegate {
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
		dataDispatcher = new FileTransferAgentDispatcher();
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

	/**
	 * Liste des routes (circuits d'attente et piste d'atterissage)
	 */
	private ArrayList<Route> routes;

	/**
	 * Charge un fichier de route, le lit, le parse et ajoute une route à
	 * <code>routes</code>.
	 * 
	 * @param path
	 *            Le fichier contenant la route.
	 * @param capacity
	 *            Le nombre d'avion maximum sur la route.
	 * @throws IOException
	 */
	public void loadRoute(String path, int capacity) throws IOException {
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
			if(instruction.isEmpty()) {
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

		routes.add(route);
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

	private boolean initDone = false;

	/**
	 * Le gestionnaire de dispatch des messages de données.
	 */
	private FileTransferAgentDispatcher dataDispatcher;

	/**
	 * Retourne l'objet de configuration de la tour.
	 */
	public Config getConfig() {
		return config;
	}

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

	// - - - Radio Delegate - - -

	/**
	 * Retourne les coordonées de la tour.
	 */
	public Coordinates getLocation() {
		return new Coordinates(0, 0, 0);
	}

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
		emit(m);
	}

	public void on(MessageData m) {
		dataDispatcher.dispatchMessageToAgent(m);
		emit(m);
	}
	
	/**
	 * Réception d'un message (cas général)
	 */
	public void on(Message m) {
		System.out.println(m);
		emit(m); // reemit
	}

	public void on(RadioEvent.PlaneConnected e) {
		// Plane connected
		emit(e); // reemit
	}

	public void on(RadioEvent.PlaneDisconnected e) {
		// Plane disconnected
		emit(e); // reemit
	}

	public void on(Event e) {
		// Unmanaged event, pass it to our own listeners
		emit(e);
	}

	private void emitDebug(String msg) {
		emitDebug(new DebugEvent(msg));
	}

	private void emitDebug(DebugEvent event) {
		if(config.getBoolean("tower.debug")) {
			emit(event);
		}
	}

	// - - - FileTransferAgent - - -

	/**
	 * Classe wrapper permettant d'utiliser des hashs comme clés.
	 */
	private class FileTransferHash {
		private byte[] hash;

		public FileTransferHash(byte[] hash) {
			this.hash = hash;
		}
		
		public boolean equals(Object o) {
			// Obvious equality
			if(this == o)
				return true;

			// Obvious inequality
			if((o == null) || (o.getClass() != this.getClass()))
				return false;

			FileTransferHash h = (FileTransferHash) o;

			return Arrays.equals(hash, h.hash);
		}

		public int hashCode() {
			return Arrays.hashCode(hash);
		}
		
		public String asHex() {
			StringBuffer sb = new StringBuffer();
			
			for(byte b : hash) {
				String h = String.format("%x", b);
				if(h.length() != 2) {
					sb.append("0");
				}
				sb.append(h);
			}
			
			return sb.toString();
		}
	}

	private class FileTransferAgentDispatcher {
		private HashMap<FileTransferHash, FileTransferAgent> agents = new HashMap<FileTransferHash, FileTransferAgent>();

		public synchronized void dispatchMessageToAgent(MessageData m) {
			FileTransferHash hash = new FileTransferHash(m.getHash());

			FileTransferAgent agent = agents.get(hash);

			if(agent == null) {
				try {
					agent = new FileTransferAgent(hash, m.getID(), m.getFormat(), m.getFileSize());
				}
				catch(Exception e) {
					e.printStackTrace();
					return;
				}

				agents.put(hash, agent);
			}

			try {
				agent.gotMessage(m);
			}
			catch(IOException e) {
				emit(new DebugEvent("Failed to write data block from " + m.getID() + " file transfer aborted"));
				try {
					agent.abort();
					agents.remove(hash);
				}
				catch(IOException e1) {
					e1.printStackTrace();
				}
			}
		}
	}

	private class FileTransferAgent {
		private FileTransferHash hash;
		private int segmentCount;

		private DataFile file;

		private FileTransferAgent(FileTransferHash hash, RadioID sender, String format, int size) throws NoSuchAlgorithmException, IOException {
			this.hash = hash;

			segmentCount = DataFile.segmentsCountForSize(size);

			String filename = sender + "-" + hash.asHex() + "." + format;
			filename = filename.replaceAll("[:/\\\\]", "_");
			String path = config.getString("tower.downloads") + filename;

			emitDebug("Started receiving file: " + path);

			file = new DataFile(path);

			// TODO: implements timeouts
		}

		public void abort() throws IOException {
			file.close();
			file.delete();
		}

		private void gotMessage(MessageData m) throws IOException {
			file.writeSegment(m.getContinuation(), m.getPayload());
		}
	}
}
