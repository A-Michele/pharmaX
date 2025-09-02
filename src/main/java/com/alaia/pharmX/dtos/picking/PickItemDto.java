package com.alaia.pharmX.dtos.picking;

import com.alaia.pharmX.models.picking.PickListItemStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PickItemDto {

	private long id;
	private String code;
	private PickListItemStatus state;
	private int quantityToPick;
	private int quantityPicked;
	private String slotsCode;
	private String nationalCode;
	private String nameProduct;
	private String reason;
	private String pickingSequence;
	private String codeOrder;
	private String lineNumber;

}
