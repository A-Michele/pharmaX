package com.alaia.pharmX.servicesImpl.exceptions;

public class CannotDeleteCustomerWithOpenOrdersException extends RuntimeException  {
	private String message;

    public CannotDeleteCustomerWithOpenOrdersException(String msg) {
        super(msg);
        this.message = msg;
    }
}
