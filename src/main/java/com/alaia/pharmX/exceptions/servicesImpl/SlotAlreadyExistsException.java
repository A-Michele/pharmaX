package com.alaia.pharmX.exceptions.servicesImpl;

public class SlotAlreadyExistsException extends RuntimeException  {
	private String message;

    public SlotAlreadyExistsException(String msg) {
        super(msg);
        this.message = msg;
    }
}
