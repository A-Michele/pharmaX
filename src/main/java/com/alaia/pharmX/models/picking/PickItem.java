package com.alaia.pharmX.models.picking;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PickItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@EqualsAndHashCode.Include
	private long id;

	private String code;

	@Enumerated(EnumType.STRING)
	private PickListItemStatus state;

	private int quantityToPick;
	private int quantityPicked = 0;
	private String slotsCode;
	private String nationalCode;
	private String nameProduct;
	private String reason;
	private String pickingSequence;
	private String codeOrder;
	private String lineNumber;

	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="pickList_id")
	@EqualsAndHashCode.Exclude
    @ToString.Exclude
    @JsonBackReference
    private PickList pickList;
}
