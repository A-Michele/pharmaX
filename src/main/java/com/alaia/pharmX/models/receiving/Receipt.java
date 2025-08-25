package com.alaia.pharmX.models.receiving;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.annotations.Check;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Check(constraints = "((state = 'DRAFT' AND received_at IS NULL) " +
        " OR (state IN ('VERIFIED','POSTED') AND received_at IS NOT NULL) " +
        " OR (state = 'CANCELED'))")
public class Receipt {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@EqualsAndHashCode.Include
	private long id;

    //PO number o DDT
    private String externalRef;

    private String supplierName;
    private LocalDateTime receivedAt;

    @Enumerated(EnumType.STRING)
    private ReceiptState state;

    @OneToMany(mappedBy = "receipt", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<ReceiptLine> lines = new HashSet<>();

}