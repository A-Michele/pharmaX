package com.alaia.pharmX.exceptions.servicesImpl;

public class QuantityNotAvailableException extends RuntimeException {
	private String message;

    public QuantityNotAvailableException(String msg) {
        super(msg);
        this.message = msg;
    }
}
