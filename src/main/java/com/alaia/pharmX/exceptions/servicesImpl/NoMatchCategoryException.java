package com.alaia.pharmX.exceptions.servicesImpl;

public class NoMatchCategoryException extends RuntimeException {
	private String message;

	public NoMatchCategoryException(String msg) {
		super(msg);
		this.message = msg;
	}
}
