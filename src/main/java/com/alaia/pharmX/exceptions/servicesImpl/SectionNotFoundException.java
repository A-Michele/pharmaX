package com.alaia.pharmX.exceptions.servicesImpl;

public class SectionNotFoundException extends RuntimeException  {
	private String message;

    public SectionNotFoundException(String msg) {
        super(msg);
        this.message = msg;
    }
}
