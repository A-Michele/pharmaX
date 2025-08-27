package com.alaia.pharmX.exceptions.servicesImpl;

public class ProductOutOfStockException extends RuntimeException {
	private String message;

    public ProductOutOfStockException(String msg) {
        super(msg);
        this.message = msg;
    }
}
