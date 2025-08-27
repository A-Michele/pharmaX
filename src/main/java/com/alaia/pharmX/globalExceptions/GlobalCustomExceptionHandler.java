package com.alaia.pharmX.globalExceptions;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.alaia.pharmX.exceptions.servicesImpl.CannotDeleteCustomerWithOpenOrdersException;
import com.alaia.pharmX.exceptions.servicesImpl.CannotDeleteOrderWithOpenOrdersException;
import com.alaia.pharmX.exceptions.servicesImpl.CannotDeleteProductWithOpenOrdersException;
import com.alaia.pharmX.exceptions.servicesImpl.CategoryNotFoundException;
import com.alaia.pharmX.exceptions.servicesImpl.CustomerAlreadyExistsException;
import com.alaia.pharmX.exceptions.servicesImpl.CustomerNotFoundException;
import com.alaia.pharmX.exceptions.servicesImpl.InvalidOrderOperationException;
import com.alaia.pharmX.exceptions.servicesImpl.InvalidProductConfigurationException;
import com.alaia.pharmX.exceptions.servicesImpl.InvalidSlotConfigurationException;
import com.alaia.pharmX.exceptions.servicesImpl.InvalidStateTransitionException;
import com.alaia.pharmX.exceptions.servicesImpl.InvalidUpdateQuantityException;
import com.alaia.pharmX.exceptions.servicesImpl.LinesVerifiedException;
import com.alaia.pharmX.exceptions.servicesImpl.LotNotFoundException;
import com.alaia.pharmX.exceptions.servicesImpl.NoMatchCategoryException;
import com.alaia.pharmX.exceptions.servicesImpl.NonCompliantQuantityException;
import com.alaia.pharmX.exceptions.servicesImpl.OrderAlreadyExistsException;
import com.alaia.pharmX.exceptions.servicesImpl.OrderLineNotFoundException;
import com.alaia.pharmX.exceptions.servicesImpl.OrderNotFoundException;
import com.alaia.pharmX.exceptions.servicesImpl.ProductAlreadyExistsException;
import com.alaia.pharmX.exceptions.servicesImpl.ProductNotFoundException;
import com.alaia.pharmX.exceptions.servicesImpl.ProductOutOfStockException;
import com.alaia.pharmX.exceptions.servicesImpl.QuantityNotAvailableException;
import com.alaia.pharmX.exceptions.servicesImpl.ReceiptLineNotFoundToReceiptException;
import com.alaia.pharmX.exceptions.servicesImpl.ReceiptNotFoundException;
import com.alaia.pharmX.exceptions.servicesImpl.SectionAlreadyExistsException;
import com.alaia.pharmX.exceptions.servicesImpl.SectionNotFoundException;
import com.alaia.pharmX.exceptions.servicesImpl.SlotAlreadyExistsException;
import com.alaia.pharmX.exceptions.servicesImpl.SlotNotFoundException;
import com.alaia.pharmX.exceptions.servicesImpl.StateNotFoundException;
import com.alaia.pharmX.exceptions.servicesImpl.StockNotAvailableException;

import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;

@ControllerAdvice
@Order(2)
public class GlobalCustomExceptionHandler {

    @ExceptionHandler({
        CustomerNotFoundException.class,
        ProductNotFoundException.class,
        OrderNotFoundException.class,
        OrderLineNotFoundException.class,
        SectionNotFoundException.class,
        SlotNotFoundException.class,
        CategoryNotFoundException.class,
        StateNotFoundException.class,
        ReceiptNotFoundException.class,
        LotNotFoundException.class,
        ProductOutOfStockException.class
    })
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public ErrorResponse handleNotFound(Exception ex) {
        return new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage());
    }

    @ExceptionHandler({
        CustomerAlreadyExistsException.class,
        ProductAlreadyExistsException.class,
        OrderAlreadyExistsException.class,
        SectionAlreadyExistsException.class,
        SlotAlreadyExistsException.class,
        InvalidStateTransitionException.class,
        LinesVerifiedException.class,
        NoMatchCategoryException.class
    })
    @ResponseStatus(HttpStatus.CONFLICT)
    @ResponseBody
    public ErrorResponse handleConflict(Exception ex) {
        return new ErrorResponse(HttpStatus.CONFLICT.value(), ex.getMessage());
    }

    @ExceptionHandler({
        IllegalArgumentException.class,
        IllegalStateException.class,
        InvalidOrderOperationException.class,
        InvalidSlotConfigurationException.class,
        InvalidProductConfigurationException.class,
        InvalidUpdateQuantityException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleBadRequest(Exception ex) {
        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
    }

    @ExceptionHandler({
        StockNotAvailableException.class,
        QuantityNotAvailableException.class,
        NonCompliantQuantityException.class,
        CannotDeleteCustomerWithOpenOrdersException.class,
        CannotDeleteProductWithOpenOrdersException.class,
        CannotDeleteOrderWithOpenOrdersException.class,
        ReceiptLineNotFoundToReceiptException.class
    })
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    @ResponseBody
    public ErrorResponse handleUnprocessable(Exception ex) {
        return new ErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY.value(), ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ErrorResponse handleOthers(Exception ex) {
        return new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Unexpected error");
    }
}