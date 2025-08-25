package com.alaia.pharmX.dtos.receiving;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateLotRequest {

	private Integer quantity;
    private LocalDate expiryDate;
    private String notes;
    private String lotCode;

}
