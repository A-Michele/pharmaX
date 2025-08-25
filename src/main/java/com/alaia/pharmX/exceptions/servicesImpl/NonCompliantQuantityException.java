package com.alaia.pharmX.exceptions.servicesImpl;

public class NonCompliantQuantityException extends RuntimeException {
	private String message;

	public NonCompliantQuantityException(String msg) {
		super(msg);
		this.message = msg;
	}
}
