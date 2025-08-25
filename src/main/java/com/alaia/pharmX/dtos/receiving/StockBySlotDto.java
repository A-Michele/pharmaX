package com.alaia.pharmX.dtos.receiving;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockBySlotDto {

	private String nationalCode;
	private String slotCode;
	private Integer quantity;

}