package sat;

/**
 * Exception cataclysmique. Cette exception hérite de RuntimeException,
 * elle n'a donc pas besoin d'être déclarée dans la liste d'exception
 * d'une méthode. Elle est utilisée lorsqu'il est nécessaire d'avoir
 * une instruction fatale.
 */
public class EndOfWorldException extends RuntimeException {
	public EndOfWorldException(String message) {
		super(message);
	}
	
	private static final long serialVersionUID = 6213373596458198719L;
}
