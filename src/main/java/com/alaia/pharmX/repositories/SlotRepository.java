package com.alaia.pharmX.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.alaia.pharmX.models.Slot;

public interface SlotRepository extends JpaRepository<Slot, Long> {
}
