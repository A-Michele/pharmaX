package com.alaia.pharmX.dtos.picking;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemToPick {

	private String orderCode;
	private String pickListCode;
	private String pickItemCode;
	private String serialNumber;
	private String nameProduct;
    private String slotsCode;
    private int quantityToPicked;
    private String pickingSequence;

}
