package com.alaia.pharmX.exceptions.servicesImpl;

public class CannotDeleteCustomerWithOpenOrdersException extends RuntimeException  {
	private String message;

    public CannotDeleteCustomerWithOpenOrdersException(String msg) {
        super(msg);
        this.message = msg;
    }
}
