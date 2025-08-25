package com.alaia.pharmX.dtos.receiving;

import java.util.Set;
import com.alaia.pharmX.models.receiving.ReceiptLineStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReceiptLineDto {

    private Long id;
    private String nationalCode;
    private Integer qtyExpected;
    private Integer qtyReceived;
    private ReceiptLineStatus status;
    private String reason;
    private String putawaySlotCode;
    private Set<LotDto> lots;

}
