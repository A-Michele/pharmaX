package com.alaia.pharmX.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.alaia.pharmX.models.Slot;

public interface SlotRepository extends JpaRepository<Slot, Long> {

	Optional<Slot> findByCode(String code);

	boolean existsByCode(String code);
}
