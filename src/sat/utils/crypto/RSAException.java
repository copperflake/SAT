package sat.utils.crypto;

/**
 * Exception lancée lors de la création d'une paire de clés avec des clés
 * incompatibles ou invalides.
 */
@SuppressWarnings("serial")
public class RSAException extends Exception {
	public RSAException(String m) {
		super(m);
	}
}
