package com.alaia.pharmX.servicesImpl.exceptions;

public class SectionNotFoundException extends RuntimeException  {
	private String message;

    public SectionNotFoundException(String msg) {
        super(msg);
        this.message = msg;
    }
}
