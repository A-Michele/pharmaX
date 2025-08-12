package com.alaia.pharmX.globalExceptions;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.alaia.pharmX.servicesImpl.exceptions.CustomerAlreadyExistsException;
import com.alaia.pharmX.servicesImpl.exceptions.CustomerNotFoundException;
import com.alaia.pharmX.servicesImpl.exceptions.InvalidOrderOperationException;
import com.alaia.pharmX.servicesImpl.exceptions.OrderAlreadyExistsException;
import com.alaia.pharmX.servicesImpl.exceptions.OrderLineNotFoundException;
import com.alaia.pharmX.servicesImpl.exceptions.OrderNotFoundException;
import com.alaia.pharmX.servicesImpl.exceptions.ProductAlreadyExistsException;
import com.alaia.pharmX.servicesImpl.exceptions.ProductNotFoundException;

import org.springframework.http.HttpStatus;

@ControllerAdvice
public class GlobalCustomExceptionHandler {
	@ExceptionHandler(value = {CustomerAlreadyExistsException.class,
							   CustomerNotFoundException.class,
							   ProductAlreadyExistsException.class,
							   ProductNotFoundException.class,
							   OrderNotFoundException.class,
							   OrderLineNotFoundException.class,
							   OrderAlreadyExistsException.class,
							   InvalidOrderOperationException.class,
							   IllegalArgumentException.class
	})
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public @ResponseBody ErrorResponse handleException(Exception ex) {
		return new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage());
	}
}
