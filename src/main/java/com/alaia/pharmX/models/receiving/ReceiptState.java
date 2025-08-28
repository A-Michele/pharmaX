package com.alaia.pharmX.models.receiving;

public enum ReceiptState {

    DRAFT,     // Created but not verified
    VERIFIED,  // Quantity checks done (ok or with discrepancies)
    PUTAWAY,    // Impacted on stock (written movements)
    CANCELED;

}