package sat.plane;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import sat.GlobalCLI;
import sat.radio.engine.client.RadioClientEngine;
import sat.radio.engine.client.RadioClientTCPEngine;

public class PlaneCLI extends GlobalCLI {
	private Plane plane;

	public PlaneCLI(Plane plane, InputStream in, PrintStream out) {
		super(in, out, "Plane> ");
		this.plane = plane;
	}

	/**
	 * Affiche la configuration actuelle de l'avion.
	 */
	public void config() {
		plane.getConfig().list(out);
	}

	/**
	 * Affiche la valeur d'un élément de la configuration de l'avion.
	 * 
	 * @param key
	 *            Le paramètre de configuration à afficher.
	 */
	public void get(String key) {
		out.println(plane.getConfig().getProperty(key));
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
		plane.getConfig().setProperty(key, value);
	}

	/**
	 * Enregistre la configuration de l'avion dans un fichier.
	 * 
	 * @param path
	 *            Le chemin du fichier dans lequel écrire la configuration.
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void store(String path) throws FileNotFoundException, IOException {
		plane.getConfig().store(new FileOutputStream(path), null);
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
		plane.getConfig().load(new FileInputStream(path));
	}

	public void connect(String engineType, String arg1, String arg2) throws IOException {
		RadioClientEngine engine;

		if(engineType.isEmpty()) {
			engineType = "tcp";
		}

		if(engineType.equals("tcp")) {
			if(arg1.isEmpty())
				arg1 = "localhost"; // Default address
			if(arg2.isEmpty())
				arg2 = "6969"; // Default port

			InetAddress host = InetAddress.getByName(arg1);
			int port = Integer.parseInt(arg2);

			engine = new RadioClientTCPEngine(host, port);
		}
		else {
			out.println("Error: unknown radio engine type");
			return;
		}

		plane.connect(engine);
	}
}
