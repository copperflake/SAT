package sat.utils.crypto;

/**
 * Exception lancée lors de la création d'une paire de clés avec des clés
 * incompatibles ou invalides.
 */
public class RSAException extends Exception {
	public RSAException(String m) {
		super(m);
	}

	private static final long serialVersionUID = -5629036399032212984L;
}
