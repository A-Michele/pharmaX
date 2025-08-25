package com.alaia.pharmX.models.receiving;

import java.util.HashSet;
import java.util.Set;
import com.alaia.pharmX.models.Slot;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReceiptLine {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@EqualsAndHashCode.Include
	private long id;

	private String nationalCode;
    private Integer qtyExpected;
    private Integer qtyReceived;
    private ReceiptLineStatus status;
    private String reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slot_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Slot putawaySlot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receipt_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Receipt receipt;

    @OneToMany(mappedBy = "receiptLine", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Lot> lots = new HashSet<>();

}