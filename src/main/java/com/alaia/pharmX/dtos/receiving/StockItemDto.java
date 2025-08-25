package com.alaia.pharmX.dtos.receiving;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockItemDto {

    private String nationalCode;
    private Integer quantity;

}