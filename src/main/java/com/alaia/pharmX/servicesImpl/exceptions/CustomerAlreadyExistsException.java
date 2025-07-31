package com.alaia.pharmX.servicesImpl.exceptions;

public class CustomerAlreadyExistsException extends RuntimeException  {
	private String message;

    public CustomerAlreadyExistsException(String msg) {
        super(msg);
        this.message = msg;
    }
}
