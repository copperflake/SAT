package sat.tower;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.InetAddress;

import sat.GlobalCLI;
import sat.radio.engine.server.RadioServerEngine;
import sat.radio.engine.server.RadioServerFileEngine;
import sat.radio.engine.server.RadioServerTCPEngine;

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
	 * @param tower		La tour de contrôle contrôlée par ce CLI
	 * @param in		Le flux d'entrée du CLI
	 * @param out		Le flux de sortie du CLI
	 */
	public TowerCLI(Tower tower, InputStream in, PrintStream out) {
		super(in, out, "Tower> ");
		this.tower = tower;
	}
	
	/**
	 * Ajoute un moteur à la radio de la tour.
	 * <p>
	 * Moteurs disponibles:
	 * <ul>
	 * <li><b>"file"</b>: initialise un {@link RadioServerFileEngine} avec
	 *     <code>arg1</code> comme nom de base pour les fichiers d'écoute.
	 * <li><b>"tcp"</b>: initialise un {@link RadioServerTCPEngine} avec
	 *     <code>arg1</code> (ou 6969 si non spécifié) comme port d'écoute
	 *     sur l'interface <code>arg2</code> (par défaut toutes).
	 * </ul>
	 * <p>
	 * Si aucun moteur n'est spécifié, un moteur {@link RadioServerTCPEngine}
	 * avec ses paramètres par défaut sera créé.
	 * 
	 * @param engineType	Le type de moteur à initialiser (file / tcp) (optionel)
	 * @param arg1			Paramètre spécifique au moteur (optionel)
	 * @param arg2			Paramètre spécifique au moteur (optionel)
	 * 
	 * @throws IOException	La création d'un moteur peut générer une exception.
	 */
	public void listen(String engineType, String arg1, String arg2) throws IOException {
		RadioServerEngine engine;
		
		// Moteur par défaut
		if(engineType.isEmpty()) {
			engineType = "tcp";
		}
		
		if(engineType.equals("file")) {
			out.println("[Warning] `listen file` requires a *nix system and is depreciated!");
			engine = new RadioServerFileEngine(arg1);
		} else if(engineType.equals("tcp")) {
			if(arg1.isEmpty()) arg1 = "6969"; // Port par défaut
			int port = Integer.parseInt(arg1);
			if(!arg2.isEmpty()) {
				// Une interface a été précisée
				InetAddress iface = InetAddress.getByName(arg2);
				engine = new RadioServerTCPEngine(port, iface);
			} else {
				engine = new RadioServerTCPEngine(port);
			}
		} else {
			out.println("Error: unknown radio engine type");
			return;
		}
		
		tower.listen(engine);
	}
}
