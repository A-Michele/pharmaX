package com.alaia.pharmX.exceptions.servicesImpl;

public class ReceiptNotFoundException extends RuntimeException {
	private String message;

    public ReceiptNotFoundException(String msg) {
        super(msg);
        this.message = msg;
    }
}
