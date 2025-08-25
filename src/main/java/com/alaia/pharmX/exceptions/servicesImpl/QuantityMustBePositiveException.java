package com.alaia.pharmX.exceptions.servicesImpl;

public class QuantityMustBePositiveException extends RuntimeException {
	private String message;

    public QuantityMustBePositiveException(String msg) {
        super(msg);
        this.message = msg;
    }
}
