package com.alaia.pharmX.exceptions.servicesImpl;

public class SlotNotFoundException  extends RuntimeException  {
	private String message;

    public SlotNotFoundException(String msg) {
        super(msg);
        this.message = msg;
    }
}