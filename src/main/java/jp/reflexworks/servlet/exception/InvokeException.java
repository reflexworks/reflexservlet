package jp.reflexworks.servlet.exception;

public class InvokeException extends Exception {

	private int httpStatus;

	public InvokeException(int httpStatus) {
		this.httpStatus = httpStatus;
	}

	public InvokeException(int httpStatus, String message) {
		super(message);
		this.httpStatus = httpStatus;
	}

	public InvokeException(int httpStatus, String message, Throwable cause) {
		super(message, cause);
		this.httpStatus = httpStatus;
	}

	public InvokeException(int httpStatus, Throwable cause) {
		super(cause);
		this.httpStatus = httpStatus;
	}
	
	public int getHttpStatus() {
		return httpStatus;
	}

	public void setHttpStatus(int httpStatus) {
		this.httpStatus = httpStatus;
	}
	
}
