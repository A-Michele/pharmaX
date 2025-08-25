package com.alaia.pharmX.exceptions.servicesImpl;

public class StateNotFoundException  extends RuntimeException  {
	private String message;

    public StateNotFoundException(String msg) {
        super(msg);
        this.message = msg;
    }
}
