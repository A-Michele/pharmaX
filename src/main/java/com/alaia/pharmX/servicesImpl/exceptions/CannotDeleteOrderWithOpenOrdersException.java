package com.alaia.pharmX.servicesImpl.exceptions;

public class CannotDeleteOrderWithOpenOrdersException extends RuntimeException  {
	private String message;

    public CannotDeleteOrderWithOpenOrdersException(String msg) {
        super(msg);
        this.message = msg;
    }
}
