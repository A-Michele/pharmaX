package com.alaia.pharmX.exceptions.servicesImpl;

public class OrderNotFoundException extends RuntimeException  {
	private String message;

    public OrderNotFoundException(String msg) {
        super(msg);
        this.message = msg;
    }
}
