package com.alaia.pharmX.servicesImpl.exceptions;

public class InvalidStateTransitionException extends RuntimeException  {
	private String message;

    public InvalidStateTransitionException(String msg) {
        super(msg);
        this.message = msg;
    }
}
