package com.alaia.pharmX.servicesImpl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.alaia.pharmX.dtos.SectionDto;
import com.alaia.pharmX.dtos.SectionUpdateDto;
import com.alaia.pharmX.exceptions.servicesImpl.SectionAlreadyExistsException;
import com.alaia.pharmX.exceptions.servicesImpl.SectionNotFoundException;
import com.alaia.pharmX.exceptions.servicesImpl.SlotNotFoundException;
import com.alaia.pharmX.mappers.SectionMapper;
import com.alaia.pharmX.mappers.SlotMapper;
import com.alaia.pharmX.models.Section;
import com.alaia.pharmX.models.Slot;
import com.alaia.pharmX.repositories.SectionRepository;
import com.alaia.pharmX.repositories.SlotRepository;
import com.alaia.pharmX.services.SectionService;

import jakarta.transaction.Transactional;

@Service
public class SectionServiceImp implements SectionService{

	@Autowired
	private SectionRepository sectionRepository;

	@Autowired
	private SlotRepository slotRepository;

	@Autowired
	private SectionMapper sectionMapper;

	@Override
	public SectionDto create(SectionDto dto) {

		if (sectionRepository.existsByCode(dto.getCode())) {
			throw new SectionAlreadyExistsException("Section already exists with code: " + dto.getCode());
		}

		Section section = sectionMapper.toEntity(dto);

        if (section.getSlots() != null) {
            Set<Slot> normalized = new HashSet<>();
            for (Slot s : section.getSlots()) {
                s.setId(0L);
                s.setSection(section);
                normalized.add(s);
            }
            section.setSlots(normalized);
        } else {
            section.setSlots(new HashSet<>());
        }

        Section saved = sectionRepository.save(section);
        return sectionMapper.toDto(saved);
	}

	@Override
	public SectionDto getById(long id) {

		Section section = sectionRepository.findById(id)
                .orElseThrow(() -> new SectionNotFoundException("Section not found with ID : " + id));
        return sectionMapper.toDto(section);
    }

	@Override
	public SectionDto getByCode(String code) {

		Section section = sectionRepository.findByCode(code)
        		.orElseThrow(() -> new SectionNotFoundException("Section not found with code : " + code));
        return sectionMapper.toDto(section);
    }

	@Override
    public List<SectionDto> getAllSection() {

		List<Section> sections = sectionRepository.findAll();
		return sections.stream()
				.map(sectionMapper::toDto)
				.toList();
	}

	@Override
	@Transactional
	public SectionDto updateNameAndCategory(long id, SectionUpdateDto dto) {

		Section section = sectionRepository.findById(id)
	            .orElseThrow(() -> new SectionNotFoundException("Section not found with ID : " + id));

	    section.setName(dto.getName());
	    section.setCategory(dto.getCategory());

	    return sectionMapper.toDto(section);
	}

	@Override
	public SectionDto delete(long id) {

		Section section = sectionRepository.findById(id)
				.orElseThrow(() -> new SectionNotFoundException("Section not found with ID : " + id));
		sectionRepository.delete(section);
		return sectionMapper.toDto(section);
	}

	@Override
	@Transactional
	public SectionDto addExistingSlot(long sectionId, long slotId) {

		Section target = sectionRepository.findById(sectionId)
	            .orElseThrow(() -> new SectionNotFoundException("Section not found with ID : " + sectionId));
	    Slot slot = slotRepository.findById(slotId)
	            .orElseThrow(() -> new SlotNotFoundException("Slot not found with ID : " + slotId));

	    if (slot.getSection() != null && slot.getSection().getId() == sectionId) {
	    	throw new IllegalStateException("Slot with ID " + slotId + " already belongs to Section with ID " + sectionId );
	    }

	    slot.setSection(target);
	    slotRepository.saveAndFlush(slot);
	    return sectionMapper.toDto(target);
	}

	@Override
	@Transactional
	public SectionDto removeSlot(long sectionId, long slotId) {

		Slot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new SlotNotFoundException("Slot not found with ID : " + slotId + " for section with ID : " + sectionId ));

        Section current = slot.getSection();
        if (current == null || current.getId() != sectionId) {
            throw new IllegalStateException(
                "Slot with ID " + slotId + " does not belong to Section with ID " + sectionId
            );
        }

        slot.setSection(null);
        slotRepository.saveAndFlush(slot);
        Section section = sectionRepository.getReferenceById(sectionId);
        section.getSlots().remove(slot);

        return sectionMapper.toDto(section);
	}
}