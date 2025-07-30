package com.alaia.pharmX.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderLineDto {
    private long id;
    private String nationalCode;
    private int quantity;

    //NON includiamo l'OrderDto per evitare loop ricorsivi
}
