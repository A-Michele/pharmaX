package com.alaia.pharmX.exceptions.servicesImpl;

public class OrderAlreadyExistsException extends RuntimeException {
	private String message;

	public OrderAlreadyExistsException(String msg) {
		super(msg);
		this.message = msg;
	}
}
