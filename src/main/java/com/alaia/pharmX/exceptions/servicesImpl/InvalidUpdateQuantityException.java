package com.alaia.pharmX.exceptions.servicesImpl;

public class InvalidUpdateQuantityException extends RuntimeException  {
	private String message;

    public InvalidUpdateQuantityException (String msg) {
        super(msg);
        this.message = msg;
    }
}
