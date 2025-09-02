package com.alaia.pharmX.models.stock;

import java.time.LocalDateTime;
import java.util.List;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
public class Stock {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@EqualsAndHashCode.Include
	private Long id;

	private String nationalCode;
	private int effectiveQuantity;
	private int reservedQuantity;

	@OneToMany(mappedBy = "stock", cascade = CascadeType.ALL, orphanRemoval = true)
	@Column(name="info_slots")
	@ToString.Exclude
    private List<InfoSlot> infoSlots;

	private LocalDateTime lastModification;

}
