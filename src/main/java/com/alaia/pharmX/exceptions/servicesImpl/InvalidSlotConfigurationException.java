package com.alaia.pharmX.exceptions.servicesImpl;

public class InvalidSlotConfigurationException extends RuntimeException  {
	private String message;

    public InvalidSlotConfigurationException(String msg) {
        super(msg);
        this.message = msg;
    }
}
