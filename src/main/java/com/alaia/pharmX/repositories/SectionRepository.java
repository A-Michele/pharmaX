package com.alaia.pharmX.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.alaia.pharmX.models.Section;

public interface SectionRepository extends JpaRepository<Section, Long> {
}
