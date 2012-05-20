package sat.utils.cli;

import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Vector;

/**
 * Framework de gestion de l'interface en ligne de commande.
 * <p>
 * La classe CLI est destinée à être étendue par une classe spécifiant l'API CLI
 * via ses méthodes publiques. Cette classe utilisera alors les méthodes
 * d'introspection de Java pour déterminer les méthodes implémentées dans la
 * classe dérivée et mettre en place l'interface CLI.
 * <p>
 * Les commandes CLI utiliseront le même nom que les méthodes définie dans les
 * classes dérivée et les paramètres seront passé à la méthode en tant que
 * paramètre de type <code>String</code>. Les méthodes publiques des classes
 * dérivées ne doivent donc accepter qu'un nombre variable d'objet
 * <code>String</code> et aucun autre type de paramètre.
 * <p>
 * Si le nombre de paramètres passés dans la ligne de commande ne correspond pas
 * au nombre de paramètre de la méthode Java, des chaines vides seront ajoutées
 * pour compléter les arguments manquants. À l'inverse si plus d'arguments sont
 * fourni que nécessaire, les arguments superflus seront ingorés.
 */
public abstract class CLI implements Runnable {
	/**
	 * Le flux d'entrée sur lequel seront lue les commandes CLI.
	 */
	protected Scanner in;

	/**
	 * Le flux de sortie utilisé par le CLI.
	 */
	protected PrintStream out;

	/**
	 * Le prompt actuel du CLI, c'est à dire la chaine de caractères affichée
	 * avant chaque commande.
	 */
	private String prompt;

	/**
	 * Le prompt par défaut, c'est à dire celui défini lors de la création du
	 * CLI. Cela permet, si le prompt a été modifié depuis, de le restaurer à
	 * son état original.
	 */
	private String prompt_default;

	/**
	 * L'api du CLI. Il s'agit simplement de la liste des méthodes publiques
	 * disponibles dans les casses filles lors de la création du CLI.
	 */
	private HashMap<String, Method> api = new HashMap<String, Method>();

	/**
	 * Indicateur d'état du CLI. Si défini à <code>true</code> le CLI ne
	 * bouclera pas après l'execution de la commande.
	 */
	private boolean exit = false;

	/**
	 * Construit un nouveau CLI écoutant et écrivant sur des flux spécifiques et
	 * avec un prompt par défaut différent de "> ".
	 * 
	 * @param in
	 *            Le flux de lecture du CLI.
	 * @param out
	 *            Le flux d'écriture du CLI.
	 * @param prompt
	 *            Le prompt par défaut de ce CLI.
	 */
	public CLI(InputStream in, PrintStream out, String prompt) {
		this.in = new Scanner(in);
		this.out = out;

		this.prompt = this.prompt_default = prompt;

		buildAPI(this.getClass());
	}

	/**
	 * Construit un nouveau CLI écoutant et écrivant sur des flux spécifiques et
	 * avec le prompt par défaut ("> ").
	 * 
	 * @param in
	 *            Le flux de lecture du CLI.
	 * @param out
	 *            Le flux d'écriture du CLI.
	 */
	public CLI(InputStream in, PrintStream out) {
		this(in, out, "> ");
	}

	/**
	 * Construit l'API du CLI en scannant la hiérarchie de classes. Cette
	 * méthode est appelée recursivement.
	 * 
	 * @param root
	 *            La classe racine à partir de laquelle le scan débute. Les
	 *            classes parentes seront alors parcourue jusqu'à atteindre la
	 *            classe <code>CLI</code>.
	 */
	private void buildAPI(Class<?> root) {
		Method[] methods = root.getDeclaredMethods();

		for(Method method : methods) {
			// Skip non-public methods
			if((method.getModifiers() & Modifier.PUBLIC) == 0)
				continue;

			// Classes-tree is scanned from leafs to main.
			// We only keep the first defined method.
			if(!api.containsKey(method.getName()))
				api.put(method.getName(), method);
		}

		Class<?> superclass = root.getSuperclass();

		if(superclass != null && superclass != CLI.class && superclass != Object.class)
			buildAPI(superclass);
	}

	/**
	 * Démarre le CLI.
	 */
	public void run() {
		while(!exit) {
			out.print(prompt);

			if(!in.hasNextLine())
				break;

			String line = in.nextLine();
			eval(line);
		}
	}

	/**
	 * Méthode utilitaire permettant de démarrer le CLI dans un nouveau thread.
	 * 
	 * @return L'objet thread de ce CLI.
	 */
	public Thread runInNewThread() {
		Thread thread = new Thread(this);
		thread.start();
		return thread;
	}

	/**
	 * Découpe la chaine passée en paramètre en prenant en compte les valeurs
	 * entre guillemets. La chaine est découpée à chaque espace blanc.
	 * 
	 * @param line
	 *            La ligne à découper.
	 * 
	 * @return Un tableau contenant chaque morceau de la chaine de caractères
	 *         initiale.
	 */
	private String[] split(String line) {
		Vector<String> parts = new Vector<String>();
		StringBuffer buffer = new StringBuffer();

		// In string mode, we dont split on whitespace.
		boolean string_mode = false;

		// In escape mode, we ignore the special meaning of the next character.
		boolean escape_mode = false;

		int line_length = line.length();

		for(int i = 0; i < line_length; i++) {
			// Current char
			char c = line.charAt(i);

			// If escape mode, simply eat the character...
			if(escape_mode) {
				buffer.append(c);
				escape_mode = false;
				continue;
			}

			// Specials chars handling
			switch(c) {
				case '\\':
					// Backslashes activate escape mode.
					escape_mode = true;
					break;

				case '"':
					// Quotes toggle string mode.
					// It's simply just a toggle! Weird uses possible.
					string_mode = !string_mode;
					break;

				case ' ':
				case '\t':
					// Oh! It's a whitespace, so we split here!
					if(!string_mode) {
						// If nothing is buffered, it's
						// a whitespace after a whitespace.
						if(buffer.length() > 0) {
							parts.add(buffer.toString());
							buffer.setLength(0);
						}

						break;
					}

				default:
					// No special meaning -> buffer!
					buffer.append(c);
			}
		}

		// If buffer isn't empty (last part) we add it to parts.
		if(buffer.length() > 0) {
			parts.add(buffer.toString());
		}

		// Return a String[] and not a Vector<String>.
		return parts.toArray(new String[parts.size()]);
	}

	/**
	 * Execute une ligne de commande.
	 * 
	 * @param line
	 *            La ligne de commande à executer.
	 */
	public void eval(String line) {
		if(line == null)
			return;

		String[] parts = split(line);

		if(parts.length == 0) {
			// Input is empty
			return;
		}

		int args_counts = parts.length - 1; // Ignore command

		String cmd = parts[0];

		// Command not in API
		if(!api.containsKey(cmd)) {
			out.println("Unknow command: " + cmd);
			return;
		}

		Method method = api.get(cmd);

		// Number of arguments required
		int args_required = method.getParameterTypes().length;

		String[] args = new String[args_required];
		for(int i = 0; i < args_required; i++) {
			if(i >= args_counts) {
				args[i] = "";
			}
			else {
				args[i] = parts[i + 1];
			}
		}

		// Eval!
		try {
			method.invoke(this, (Object[]) args);
		}
		catch(InvocationTargetException e) {
			// The call-stack for InvocationTargetException is always the
			// same. So, ignore it and print TargetException.
			e.getTargetException().printStackTrace(out);
		}
		catch(Exception e) {
			e.printStackTrace(out);
		}
	}

	/**
	 * Retourne le prompt actuel du CLI.
	 */
	public String prompt() {
		return prompt;
	}

	/**
	 * Modifie le prompt actif du CLI. Le prompt orginal n'est pas modifié et
	 * peut être réactivé avec <code>restore>Prompt()</code>.
	 */
	public void setPrompt(String newPrompt) {
		prompt = newPrompt;
	}

	/**
	 * Annule les changements apportés par <code>setPrompt()</code> et réactive
	 * le prompt original défini lors de la création du CLI.
	 */
	public void restorePrompt() {
		prompt = prompt_default;
	}

	/**
	 * Racourci pour afficher une ligne sur la sortie du CLI.
	 * 
	 * @param s
	 *            La ligne à afficher.
	 */
	public void println(String s) {
		out.println(s);
	}

	public void print(String s) {
		out.print(s);
	}

	/**
	 * Termine le CLI.
	 */
	public void exit() {
		exit = true;
		in.close();
	}
}
