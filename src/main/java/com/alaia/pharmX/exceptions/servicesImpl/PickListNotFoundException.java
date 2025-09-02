package com.alaia.pharmX.exceptions.servicesImpl;

public class PickListNotFoundException  extends RuntimeException {
	private String message;

    public PickListNotFoundException(String msg) {
        super(msg);
        this.message = msg;
    }
}
