package com.alaia.pharmX.exceptions.servicesImpl;

public class LinesVerifiedException extends RuntimeException  {
	private String message;

    public LinesVerifiedException(String msg) {
        super(msg);
        this.message = msg;
    }
}
