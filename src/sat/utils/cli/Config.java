package sat.utils.cli;

import java.util.Properties;

/**
 * Gestionnaire de configuration.
 */
public class Config extends Properties {
	/**
	 * Crée un gestionnaire de configuration vide.
	 */
	public Config() {
	}

	/**
	 * Crée un gestionnaire de configuration avec des valeurs par défauts.
	 * 
	 * @param defaults
	 *            Les valeurs par défaut de ce gestionnaire de configuration.
	 */
	public Config(Properties defaults) {
		super(defaults);
	}

	/**
	 * Lis un paramètre en tant que boolean. Les valeurs <code>null</code> (non
	 * défini), <code>false</code>, <code>no</code> et <code>off</code> sont
	 * considérées comme fausses. Toutes les autres valeurs comme vraies.
	 * 
	 * @param key
	 *            Le nom du paramètre à lire.
	 */
	public boolean getBoolean(String key) {
		String value = getProperty(key);

		if(value == null)
			return false;

		return !(value.equals("false") || value.equals("no") || value.equals("off"));
	}

	/**
	 * Lis un paramètre en tant que chaine de caractère.
	 * 
	 * @param key
	 *            Le nom du paramètre à lire.
	 */
	public String getString(String key) {
		return getProperty(key);
	}

	/**
	 * Lis un paramètre en tant que nombre <code>Integer</code>.
	 * 
	 * @param key
	 *            Le nom du paramètre à lire.
	 */
	public int getInt(String key) {
		return key == null ? 0 : Integer.parseInt(getProperty(key));
	}

	private static final long serialVersionUID = -7067941402784421244L;
}
