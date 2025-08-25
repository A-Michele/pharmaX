package com.alaia.pharmX.exceptions.servicesImpl;

public class ProductAlreadyExistsException extends RuntimeException {
	private String message;

    public ProductAlreadyExistsException(String msg) {
        super(msg);
        this.message = msg;
    }
}
