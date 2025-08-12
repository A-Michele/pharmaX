package com.alaia.pharmX.servicesImpl.exceptions;

public class ProductNotFoundException extends RuntimeException {
	private String message;

    public ProductNotFoundException(String msg) {
        super(msg);
        this.message = msg;
    }
}
