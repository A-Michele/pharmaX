package com.alaia.pharmX.exceptions.servicesImpl;

public class PickItemNotFound extends RuntimeException {
	private String message;

	public PickItemNotFound(String msg) {
		super(msg);
		this.message = msg;
	}
}
