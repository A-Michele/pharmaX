package com.alaia.pharmX.dtos.receiving;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class putAwayReceiptResponse {

    private Long receiptId;
    private int movementCount;
    private LocalDateTime postedAt;

}
