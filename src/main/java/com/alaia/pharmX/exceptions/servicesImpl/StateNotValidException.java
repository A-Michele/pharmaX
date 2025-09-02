package com.alaia.pharmX.exceptions.servicesImpl;

public class StateNotValidException extends RuntimeException  {
	private String message;

    public StateNotValidException(String msg) {
        super(msg);
        this.message = msg;
    }
}
