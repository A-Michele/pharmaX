package com.alaia.pharmX.models.receiving;

public enum ReceiptState {

    DRAFT,     // Created but not verified
    VERIFIED,  // Quantity checks done (ok or with discrepancies)
    POSTED,    // Impacted on stock (written movements)
    CANCELED;

	//To check if a state change is valid
    public boolean canTransitionTo(ReceiptState next) {
        return switch (this) {
            case DRAFT -> next == VERIFIED || next == CANCELED;
            case VERIFIED -> next == POSTED || next == CANCELED;
            case POSTED, CANCELED -> false;
        };
    }

}