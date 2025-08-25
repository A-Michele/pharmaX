package com.alaia.pharmX.models.receiving;

public enum MovementType {

	INBOUND_RECEIPT,  //Arrival of products to be stored
    ORDER_ALLOCATION, //For the execution of an order
    PICKING,		  //To complete orders
    ADJUSTMENT,       //Adding/removing of receiving/orders
    RETURN			  //Indicates returns or cancellations

}