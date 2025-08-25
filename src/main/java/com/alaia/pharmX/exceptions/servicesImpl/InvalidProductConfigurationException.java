package com.alaia.pharmX.exceptions.servicesImpl;

public class InvalidProductConfigurationException extends RuntimeException  {
	private String message;

    public InvalidProductConfigurationException(String msg) {
        super(msg);
        this.message = msg;
    }
}

