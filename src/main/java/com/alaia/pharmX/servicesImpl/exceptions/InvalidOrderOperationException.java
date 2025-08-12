package com.alaia.pharmX.servicesImpl.exceptions;

public class InvalidOrderOperationException extends RuntimeException  {
	private String message;

    public InvalidOrderOperationException(String msg) {
        super(msg);
        this.message = msg;
    }
}
