package com.alaia.pharmX.dtos.receiving;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStockItemDto {

	@NotBlank
    private String nationalCode;

	@NotBlank
	private Integer quantity;

	private String reason;

}
