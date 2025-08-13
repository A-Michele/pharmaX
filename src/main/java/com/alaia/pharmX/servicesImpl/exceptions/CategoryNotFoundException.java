package com.alaia.pharmX.servicesImpl.exceptions;

public class CategoryNotFoundException extends RuntimeException  {
	private String message;

    public CategoryNotFoundException(String msg) {
        super(msg);
        this.message = msg;
    }
}
