package com.alaia.pharmX.exceptions.servicesImpl;

public class CannotDeleteOrderWithOpenOrdersException extends RuntimeException  {
	private String message;

    public CannotDeleteOrderWithOpenOrdersException(String msg) {
        super(msg);
        this.message = msg;
    }
}
