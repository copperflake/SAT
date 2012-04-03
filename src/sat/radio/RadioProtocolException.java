package sat.radio;

/**
 * Une exception lancée lors d'une erreur protocolaire lors de l'échange
 * messages. Par exemple la réception d'un message d'un certain type lorsque ce
 * type n'est pas autorisé dans l'état de la connexion par le protocole.
 */
public class RadioProtocolException extends Exception {
	/**
	 * Crée une RadioProtocoleException.
	 */
	public RadioProtocolException() {
	}

	/**
	 * Crée une RadioProtocoleException avec un message d'erreur spécifique.
	 * 
	 * @param s
	 *            Le message d'erreur décrivant l'erreur de façon plus précise.
	 */
	public RadioProtocolException(String s) {
		super(s);
	}

	private static final long serialVersionUID = 6302497690665940666L;
}
