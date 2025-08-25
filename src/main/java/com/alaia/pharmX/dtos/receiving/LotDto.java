package com.alaia.pharmX.dtos.receiving;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LotDto {

	private Long id;
    private String lotCode;
    private LocalDate expiryDate;
    private String notes;
    private Integer quantity;

}