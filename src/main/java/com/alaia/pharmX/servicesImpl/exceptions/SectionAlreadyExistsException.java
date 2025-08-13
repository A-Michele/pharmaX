package com.alaia.pharmX.servicesImpl.exceptions;

public class SectionAlreadyExistsException extends RuntimeException  {
	private String message;

    public SectionAlreadyExistsException(String msg) {
        super(msg);
        this.message = msg;
    }
}