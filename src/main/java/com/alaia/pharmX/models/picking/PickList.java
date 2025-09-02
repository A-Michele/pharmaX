package com.alaia.pharmX.models.picking;

import java.time.LocalDateTime;
import java.util.List;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
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
public class PickList {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@EqualsAndHashCode.Include
	private long id;

	private String code;

	@Enumerated(EnumType.STRING)
	private PickListStatus state;

	private LocalDateTime createdAt;
	private LocalDateTime lastModification;
	private String cf;

	@OneToMany(mappedBy = "pickList", cascade = CascadeType.ALL, orphanRemoval = true)
	@Column(name="pistList_items")
	@ToString.Exclude
    private List<PickItem> items;

}
