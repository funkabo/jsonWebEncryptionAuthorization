package se.funkabo.exception.handler;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import se.funkabo.entity.response.ErrorResponse;
import se.funkabo.exception.UserNotFoundException;

@ControllerAdvice
public class UserNotFoundExceptionHandler {

	@ExceptionHandler
    public ResponseEntity<ErrorResponse> handleException(UserNotFoundException ex){
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatus(HttpStatus.NOT_FOUND.toString().toLowerCase());
        errorResponse.setMessage(ex.getMessage());
        errorResponse.setTimestamp(new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date()));
        return new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.NOT_FOUND);
    }
}
