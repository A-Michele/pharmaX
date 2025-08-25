package com.alaia.pharmX.dtos.picking;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemToPick{

	String nationalCode;
	String sectionCode;
	String sectionName;
	String slotCode;
	int quantity;
	String pickingSequence;

}
