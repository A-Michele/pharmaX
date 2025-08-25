package com.alaia.pharmX.dtos.stock;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockDto {

	private long id;
	private String nationalCode;
	private int effectiveQuantity;
	private int reservedQuantity;
	private LocalDateTime lastModification;

}