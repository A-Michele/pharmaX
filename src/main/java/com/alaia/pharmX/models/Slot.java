package com.alaia.pharmX.models;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Slot {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	private String code;
	private int volume;

	@Column(name="picking_sequence")
	private String pickingSequence;

	@ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="section_id")
    private Section section;
}
