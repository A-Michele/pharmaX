package com.alaia.pharmX.dtos.stock;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InfoSlotDto {

	private long id;
	private String slotCode;
	private int quantity;
}
