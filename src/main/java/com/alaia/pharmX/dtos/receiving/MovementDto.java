package com.alaia.pharmX.dtos.receiving;

import java.time.LocalDateTime;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovementDto{

    private Long id;

    @NotBlank
    private String nationalCode;

    private Integer quantity;
    private String type;
    private String referenceType;
    private Long referenceId;

    private String slotCode;

    private LocalDateTime timestamp;

}