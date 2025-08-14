package com.alaia.pharmX.servicesImpl.exceptions;

public class CannotDeleteProductWithOpenOrdersException extends RuntimeException  {
	private String message;

    public CannotDeleteProductWithOpenOrdersException(String msg) {
        super(msg);
        this.message = msg;
    }
}
