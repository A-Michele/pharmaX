package com.alaia.pharmX.exceptions.servicesImpl;

public class SectionAlreadyExistsException extends RuntimeException  {
	private String message;

    public SectionAlreadyExistsException(String msg) {
        super(msg);
        this.message = msg;
    }
}