package com.alaia.pharmX.servicesImpl.exceptions;

public class SlotAlreadyExistsException extends RuntimeException  {
	private String message;

    public SlotAlreadyExistsException(String msg) {
        super(msg);
        this.message = msg;
    }
}
