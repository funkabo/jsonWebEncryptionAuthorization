package se.funkabo.exception.handler;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import se.funkabo.entity.response.ErrorResponse;


@ControllerAdvice
public class IncorrectPasswordExceptionHandler {

	@ExceptionHandler
    public ResponseEntity<ErrorResponse> handleException(BadCredentialsException ex){
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatus(HttpStatus.PRECONDITION_FAILED.toString().toLowerCase());
        errorResponse.setMessage(ex.getMessage());
        errorResponse.setTimestamp(new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date()));
        return new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.PRECONDITION_FAILED);
    }
}
