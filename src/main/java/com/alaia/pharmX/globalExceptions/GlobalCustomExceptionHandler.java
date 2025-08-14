package com.alaia.pharmX.globalExceptions;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.alaia.pharmX.servicesImpl.exceptions.CannotDeleteCustomerWithOpenOrdersException;
import com.alaia.pharmX.servicesImpl.exceptions.CannotDeleteOrderWithOpenOrdersException;
import com.alaia.pharmX.servicesImpl.exceptions.CannotDeleteProductWithOpenOrdersException;
import com.alaia.pharmX.servicesImpl.exceptions.CategoryNotFoundException;
import com.alaia.pharmX.servicesImpl.exceptions.CustomerAlreadyExistsException;
import com.alaia.pharmX.servicesImpl.exceptions.CustomerNotFoundException;
import com.alaia.pharmX.servicesImpl.exceptions.InvalidOrderOperationException;
import com.alaia.pharmX.servicesImpl.exceptions.InvalidStateTransitionException;
import com.alaia.pharmX.servicesImpl.exceptions.OrderAlreadyExistsException;
import com.alaia.pharmX.servicesImpl.exceptions.OrderLineNotFoundException;
import com.alaia.pharmX.servicesImpl.exceptions.OrderNotFoundException;
import com.alaia.pharmX.servicesImpl.exceptions.ProductAlreadyExistsException;
import com.alaia.pharmX.servicesImpl.exceptions.ProductNotFoundException;
import com.alaia.pharmX.servicesImpl.exceptions.SectionAlreadyExistsException;
import com.alaia.pharmX.servicesImpl.exceptions.SectionNotFoundException;
import com.alaia.pharmX.servicesImpl.exceptions.SlotAlreadyExistsException;
import com.alaia.pharmX.servicesImpl.exceptions.SlotNotFoundException;
import com.alaia.pharmX.servicesImpl.exceptions.StateNotFoundException;

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
							   IllegalArgumentException.class,
							   SectionAlreadyExistsException.class,
							   SectionNotFoundException.class,
							   SlotNotFoundException.class,
							   SlotAlreadyExistsException.class,
							   IllegalStateException.class,
							   CategoryNotFoundException.class,
							   StateNotFoundException.class,
							   InvalidStateTransitionException.class,
							   CannotDeleteCustomerWithOpenOrdersException.class,
							   CannotDeleteProductWithOpenOrdersException.class,
							   CannotDeleteOrderWithOpenOrdersException.class
	})

	@ResponseStatus(HttpStatus.NOT_FOUND)
	public @ResponseBody ErrorResponse handleException(Exception ex) {
		return new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage());
	}
}
