package sat;

import java.io.InputStream;
import java.io.PrintStream;

import sat.utils.cli.CLI;

/**
 * Interface CLI globale au projet SAT. Propose certaines méthodes utilitaires
 * utilisées par tous les sous-projets.
 */
public abstract class GlobalCLI extends CLI {
	/**
	 * Proxy vers le constructeur parent, avec prompt par défaut
	 * 
	 * @param in
	 *            Le flux d'entrée du CLI
	 * @param out
	 *            Le flux de sortie du CLI
	 */
	public GlobalCLI(InputStream in, PrintStream out) {
		super(in, out, "SAT> ");
	}

	/**
	 * Proxy vers le constructeur parent
	 * 
	 * @param in
	 *            Le flux d'entrée du CLI
	 * @param out
	 *            Le flux de sortie du CLI
	 * @param prompt
	 *            Le prompt affiché au début de chaque ligne
	 */
	public GlobalCLI(InputStream in, PrintStream out, String prompt) {
		super(in, out, prompt);
	}

	/**
	 * Stop le CLI et termine la JVM.
	 */
	public void exit() {
		super.exit();
		Runtime.getRuntime().exit(0);
	}
}
