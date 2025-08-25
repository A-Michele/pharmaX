package com.alaia.pharmX.exceptions.servicesImpl;

public class StockNotAvailableException  extends RuntimeException  {
	private String message;

    public StockNotAvailableException(String msg) {
        super(msg);
        this.message = msg;
    }
}
