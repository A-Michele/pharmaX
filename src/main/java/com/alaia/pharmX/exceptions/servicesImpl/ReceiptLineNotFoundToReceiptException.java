package com.alaia.pharmX.exceptions.servicesImpl;

public class ReceiptLineNotFoundToReceiptException extends RuntimeException {
	private String message;

    public ReceiptLineNotFoundToReceiptException(String msg) {
        super(msg);
        this.message = msg;
    }
}
