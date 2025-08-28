package com.alaia.pharmX.dtos.receiving;

import java.time.LocalDateTime;
import java.util.Set;
import com.alaia.pharmX.models.receiving.ReceiptState;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReceiptDto {

    private Long id;
    private String externalRef;
    private String supplierName;
    private LocalDateTime receivedAt;
    private LocalDateTime lastModification;
    private ReceiptState state;
    private String notes;
    private Set<ReceiptLineDto> lines;

}