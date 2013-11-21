package tw.com.ischool.oneknow;

public class OneKnowException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String _message;

	public OneKnowException(String message, Throwable cause) {
		super(cause);
		_message = message;
	}

	public OneKnowException(String message) {
		super();
		_message = message;
	}

	@Override
	public String getMessage() {
		return _message;
	}
}
