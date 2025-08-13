package com.alaia.pharmX.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.alaia.pharmX.models.Section;

public interface SectionRepository extends JpaRepository<Section, Long> {

	Optional<Section> findByCode(String code);

	boolean existsByCode(String code);
}
