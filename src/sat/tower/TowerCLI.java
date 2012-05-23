package sat.tower;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.InetAddress;

import sat.DebugEvent;
import sat.GlobalCLI;
import sat.events.Event;
import sat.events.EventListener;
import sat.gui.GUI;
import sat.radio.RadioEvent;
import sat.radio.engine.server.RadioServerEngine;
import sat.radio.engine.server.RadioServerTCPEngine;
import sat.tower.agent.AgentResult;
import sat.tower.agent.AgentRequest;
import sat.tower.agent.AgentServer;
import sat.tower.agent.TowerAgent;
import sat.utils.crypto.RSAKey;
import sat.utils.crypto.RSAKeyPair;

/**
 * Interface CLI de la tour de contrôle.
 */
public class TowerCLI extends GlobalCLI implements EventListener {
	/**
	 * L'agent de la tour controlée par cette instance de TowerCLI
	 */
	private TowerAgent agent;

	public TowerCLI(InputStream i, PrintStream o) {
		this(i, o, new TowerAgent());
	}

	/**
	 * Crée un nouveau CLI de tour de contrôle.
	 * 
	 * @param tower
	 *            La tour de contrôle contrôlée par ce CLI
	 * @param i
	 *            Le flux d'entrée du CLI
	 * @param o
	 *            Le flux de sortie du CLI
	 */
	public TowerCLI(InputStream i, PrintStream o, TowerAgent agent) {
		super(i, o, "Tower> ");

		this.agent = agent;
		agent.addListener(this);
	}

	private Tower getTower() {
		if(agent.isRemote()) {
			throw new RuntimeException("This command cannot be used in a remote CLI");
		}

		return Tower.getInstance();
	}

	public void init() {
		agent.requestInit();
	}

	/**
	 * Affiche la configuration actuelle de la tour.
	 */
	public void config() {
		setPaused(true); // MUST BE HERE, LOCAL REQUESTS ARE SYNCHRONOUS
		agent.requestConfig(new EventListener() {
			@SuppressWarnings("unused")
			public void on(AgentResult.ConfigResult ev) {
				ev.getConfig().list(out);
				setPaused(false);
			}
		});
	}

	/**
	 * Affiche la valeur d'un élément de la configuration de la tour.
	 * 
	 * @param key
	 *            Le paramètre de configuration à afficher.
	 */
	public void get(String key) {
		setPaused(true);
		agent.requestConfigGetKey(key, new EventListener() {
			@SuppressWarnings("unused")
			public void on(AgentResult.ConfigGetKeyResult ev) {
				println(ev.getValue());
				setPaused(false);
			}
		});
	}

	/**
	 * Défini la valeur d'un paramètre de configuration.
	 * 
	 * @param key
	 *            Le nom du paramètre à définir.
	 * @param value
	 *            La valeur du paramètre.
	 */
	public void set(String key, String value) {
		agent.requestConfigSetKey(key, value);
	}

	/**
	 * Enregistre la configuration de la tour dans un fichier.
	 * 
	 * @param path
	 *            Le chemin du fichier dans lequel écrire la configuration.
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void store(String path) throws FileNotFoundException, IOException {
		getTower().getConfig().store(new FileOutputStream(path), null);
	}

	/**
	 * Charge une configuration depuis un fichier créé par la commande
	 * <code>store</code>.
	 * 
	 * @param path
	 *            Le chemin du fichier à lire.
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void load(String path) throws FileNotFoundException, IOException {
		getTower().getConfig().load(new FileInputStream(path));
	}

	/**
	 * Charge une route.
	 * 
	 * @param path
	 *            Le fichier contenant la route.
	 * @param capacity
	 *            Le nombre d'avion maximum sur la route.
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	public void loadroute(String path, String capacity) throws NumberFormatException, IOException {
		getTower().loadRoute(path, Integer.parseInt(capacity));
	}

	/**
	 * Ajoute un moteur à la radio de la tour.
	 * <p>
	 * Moteurs disponibles:
	 * <ul>
	 * <li><b>"file"</b>: initialise un {@link RadioServerFileEngine} avec
	 * <code>arg1</code> comme nom de base pour les fichiers d'écoute.
	 * <li><b>"tcp"</b>: initialise un {@link RadioServerTCPEngine} avec
	 * <code>arg1</code> (ou 6969 si non spécifié) comme port d'écoute sur
	 * l'interface <code>arg2</code> (par défaut toutes).
	 * </ul>
	 * <p>
	 * Si aucun moteur n'est spécifié, un moteur {@link RadioServerTCPEngine}
	 * avec ses paramètres par défaut sera créé.
	 * 
	 * @param engineType
	 *            Le type de moteur à initialiser (file / tcp) (optionel)
	 * @param arg1
	 *            Paramètre spécifique au moteur (optionel)
	 * @param arg2
	 *            Paramètre spécifique au moteur (optionel)
	 * 
	 * @throws IOException
	 *             La création d'un moteur peut générer une exception. Cette
	 *             exception est passée au code appelant.
	 */
	public void listen(String engineType, String arg1, String arg2) throws IOException {
		Tower tower = getTower();

		RadioServerEngine engine;

		// Moteur par défaut
		if(engineType.isEmpty()) {
			engineType = "tcp";
		}

		if(engineType.equals("tcp")) {
			if(arg1.isEmpty())
				arg1 = "6969"; // Port par défaut
			int port = Integer.parseInt(arg1);
			if(!arg2.isEmpty()) {
				// Une interface a été précisée
				InetAddress iface = InetAddress.getByName(arg2);
				engine = new RadioServerTCPEngine(port, iface);
			}
			else {
				engine = new RadioServerTCPEngine(port);
			}
		}
		else {
			out.println("Error: unknown radio engine type");
			return;
		}

		tower.listen(engine);
	}

	public void gui() {
		/**
		 * AirportGUI(HD) Changez HD en "false" si la vue 3D est trop lente.
		 */
		// TODO: start GUI with CLI's agent
		new GUI(true, agent);
	}

	public void agentserver() {
		if(agent.isRemote()) {
			println("You cannot start an AgentServer from a remote CLI");
			return;
		}

		if(AgentServer.isRunning()) {
			println("AgentServer is already running");
		}
		else {
			AgentServer.start();
			println("AgentServer started");
		}
	}

	public void writekey(final String path) {
		agent.requestTowerKey(new EventListener() {
			@SuppressWarnings("unused")
			public void on(AgentResult.TowerKeyResult ev) throws IOException {
				RSAKey key = ev.getKey();

				FileOutputStream fos = new FileOutputStream(path);
				DataOutputStream dos = new DataOutputStream(fos);

				dos.writeInt(key.getLength());

				dos.writeInt(key.getModulus().toByteArray().length);
				dos.write(key.getModulus().toByteArray());

				dos.writeInt(key.getExponent().toByteArray().length);
				dos.write(key.getExponent().toByteArray());

				dos.close();
				fos.close();
			}
		});
	}

	public void on(DebugEvent event) {
		print("[DEBUG] ");
		println(event.getMessage());
	}

	public void on(RadioEvent.UncaughtException event) {
		print("[EXCEPTION] ");
		event.getException().printStackTrace(out);
	}
}
