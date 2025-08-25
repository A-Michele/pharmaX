package com.alaia.pharmX.exceptions.servicesImpl;

public class CustomerNotFoundException extends RuntimeException  {
	private String message;

    public CustomerNotFoundException(String msg) {
        super(msg);
        this.message = msg;
    }
}
