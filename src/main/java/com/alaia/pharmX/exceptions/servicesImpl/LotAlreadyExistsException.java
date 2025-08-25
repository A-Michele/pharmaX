package com.alaia.pharmX.exceptions.servicesImpl;

public class LotAlreadyExistsException extends RuntimeException  {
	private String message;

    public LotAlreadyExistsException(String msg) {
        super(msg);
        this.message = msg;
    }
}
