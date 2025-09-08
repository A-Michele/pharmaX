package com.alaia.pharmX.exceptions.servicesImpl;

public class CannotDeleteOrderWithNoStateOpenException extends RuntimeException  {
	private String message;

    public CannotDeleteOrderWithNoStateOpenException(String msg) {
        super(msg);
        this.message = msg;
    }
}
