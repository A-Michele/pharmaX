package com.alaia.pharmX.servicesImpl.exceptions;

public class ProductAlreadyExistsException extends RuntimeException {
	private String message;

    public ProductAlreadyExistsException(String msg) {
        super(msg);
        this.message = msg;
    }
}
