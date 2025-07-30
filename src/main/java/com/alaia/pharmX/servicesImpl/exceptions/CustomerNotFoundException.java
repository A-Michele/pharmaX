package com.alaia.pharmX.servicesImpl.exceptions;

public class CustomerNotFoundException extends RuntimeException  {
	private String message;

    public CustomerNotFoundException(String msg) {
        super(msg);
        this.message = msg;
    }
}
