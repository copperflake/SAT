package sat;

/**
 * Exception cataclysmique. Cette exception hérite de RuntimeException, elle n'a
 * donc pas besoin d'être déclarée dans la liste d'exception d'une méthode. Elle
 * est utilisée lorsqu'il est nécessaire d'avoir une instruction fatale.
 * 
 * TODO: Avec de multiples threads, cette exception n'est pas forcément fatale.
 */
@SuppressWarnings("serial")
public class EndOfWorldException extends RuntimeException {
	public EndOfWorldException(String message) {
		super(message);
	}

	public EndOfWorldException() {
	}
}
