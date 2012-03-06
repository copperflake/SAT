package sat;

public class EndOfWorldException extends RuntimeException {
	public EndOfWorldException(String message) {
		super(message+" Yeah, that's why the world implode.");
	}
	
	private static final long serialVersionUID = 6213373596458198719L;
}
