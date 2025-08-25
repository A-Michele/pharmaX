package com.alaia.pharmX.models.receiving;

public enum ReceiptLineStatus {

    OK,        // qty_received == qty_expected
    SHORT,     // qty_received < qty_expected
    OVER,      // qty_received > qty_expected
    DAMAGED    // received but with damage

}