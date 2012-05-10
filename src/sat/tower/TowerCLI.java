package sat.tower;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.InetAddress;

import sat.GlobalCLI;
import sat.radio.engine.server.RadioServerEngine;
import sat.radio.engine.server.RadioServerTCPEngine;
import sat.utils.crypto.RSAKeyPair;

/**
 * Interface CLI de la tour de contrôle.
 */
public class TowerCLI extends GlobalCLI {
	/**
	 * La tour controlée par cette instance de TowerCLI
	 */
	private Tower tower;

	/**
	 * Crée un nouveau CLI de tour de contrôle.
	 * 
	 * @param tower
	 *            La tour de contrôle contrôlée par ce CLI
	 * @param in
	 *            Le flux d'entrée du CLI
	 * @param out
	 *            Le flux de sortie du CLI
	 */
	public TowerCLI(InputStream in, PrintStream out) {
		super(in, out, "Tower> ");
		this.tower = Tower.getInstance();
	}

	/**
	 * Affiche la configuration actuelle de la tour.
	 */
	public void config() {
		tower.getConfig().list(out);
	}

	/**
	 * Affiche la valeur d'un élément de la configuration de la tour.
	 * 
	 * @param key
	 *            Le paramètre de configuration à afficher.
	 */
	public void get(String key) {
		out.println(tower.getConfig().getProperty(key));
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
		tower.getConfig().setProperty(key, value);
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
		tower.getConfig().store(new FileOutputStream(path), null);
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
		tower.getConfig().load(new FileInputStream(path));
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
		tower.startGui();
	}

	public void writekey(String path) throws IOException {
		FileOutputStream fos = new FileOutputStream(path);
		DataOutputStream dos = new DataOutputStream(fos);

		RSAKeyPair keyPair = tower.getKeyPair().makePublic();

		dos.writeInt(keyPair.keyLength());

		dos.writeInt(keyPair.getPublicKey().getModulus().toByteArray().length);
		dos.write(keyPair.getPublicKey().getModulus().toByteArray());

		dos.writeInt(keyPair.getPublicKey().getExponent().toByteArray().length);
		dos.write(keyPair.getPublicKey().getExponent().toByteArray());
	}
}
