package com.alaia.pharmX.exceptions.servicesImpl;

public class OrderAlreadyReleasedException extends RuntimeException {
	private String message;

	public OrderAlreadyReleasedException(String msg) {
		super(msg);
		this.message = msg;
	}
}
