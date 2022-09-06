package se.funkabo.exception;

import org.springframework.security.authentication.BadCredentialsException;

public class IncorrectPasswordException extends BadCredentialsException{

	private static final long serialVersionUID = 1L;

	public IncorrectPasswordException(String message) {
		super(message);
	}
	
	public IncorrectPasswordException(String message, Throwable throwable) {
		super(message, throwable);
	}

}
