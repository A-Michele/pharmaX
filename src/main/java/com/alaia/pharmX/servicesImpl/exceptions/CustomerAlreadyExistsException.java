package com.alaia.pharmX.servicesImpl.exceptions;

public class CustomerAlreadyExistsException extends RuntimeException  {
	private String message;

    public CustomerAlreadyExistsException() {
    	message = "Customer already exists with this CF";
    }

    public CustomerAlreadyExistsException(String msg) {
        super(msg);
        this.message = msg;
    }
}
