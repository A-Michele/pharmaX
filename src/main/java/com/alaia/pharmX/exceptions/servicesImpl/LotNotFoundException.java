package com.alaia.pharmX.exceptions.servicesImpl;

public class LotNotFoundException extends RuntimeException  {
	private String message;

    public LotNotFoundException(String msg) {
        super(msg);
        this.message = msg;
    }
}