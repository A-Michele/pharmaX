package com.alaia.pharmX.globalExceptions;

import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@ControllerAdvice
@Order(1)
public class GlobalExceptionHandlerValidation extends ResponseEntityExceptionHandler {

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(
	        MethodArgumentNotValidException ex, HttpHeaders headers,
	        HttpStatusCode status, WebRequest request) {

	    // Aggiungi il log per vedere se il metodo viene invocato
	    Logger logger = LoggerFactory.getLogger(GlobalExceptionHandlerValidation.class);
	    logger.error("Validation failed: " + ex.getMessage(), ex);

	    Map<String, Object> responseBody = new HashMap<>();
	    responseBody.put("timestamp", Instant.now().toString());
	    responseBody.put("status", status.value());

	    List<String> errors = ex.getBindingResult()
	            .getFieldErrors()
	            .stream()
	            .map(fieldError -> fieldError.getField() + " " + fieldError.getDefaultMessage())
	            .toList();

	    responseBody.put("errors", errors);

	    return new ResponseEntity<>(responseBody, headers, status);
	}
}