package com.alaia.pharmX.servicesImpl.exceptions;

public class OrderLineNotFoundException extends RuntimeException {
	private String message;

	public OrderLineNotFoundException(String msg) {
		super(msg);
		this.message = msg;
	}
}
