package com.alaia.pharmX.exceptions.servicesImpl;

public class ErrorUpdateQuantityException extends RuntimeException  {
	private String message;

    public ErrorUpdateQuantityException(String msg) {
        super(msg);
        this.message = msg;
    }
}

