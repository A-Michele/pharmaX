package com.alaia.pharmX.exceptions.servicesImpl;

public class InvalidStateTransitionException extends RuntimeException  {
	private String message;

    public InvalidStateTransitionException(String msg) {
        super(msg);
        this.message = msg;
    }
}
