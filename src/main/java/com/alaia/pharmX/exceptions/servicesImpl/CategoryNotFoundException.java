package com.alaia.pharmX.exceptions.servicesImpl;

public class CategoryNotFoundException extends RuntimeException  {
	private String message;

    public CategoryNotFoundException(String msg) {
        super(msg);
        this.message = msg;
    }
}
