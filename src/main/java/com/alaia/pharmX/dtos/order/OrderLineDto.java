package com.alaia.pharmX.dtos.order;

import com.alaia.pharmX.models.order.LineOrderType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderLineDto {

    private long id;

    @NotBlank
    private String nationalCode;

	@Positive
    private int quantity;

	private String lineNumber;
	private LineOrderType type;

}