package com.alaia.pharmX.exceptions.servicesImpl;

public class NoPickItemAvailableException extends RuntimeException {
	private String message;

	public NoPickItemAvailableException(String msg) {
		super(msg);
		this.message = msg;
	}
}
