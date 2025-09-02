package com.alaia.pharmX.servicesImpl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.alaia.pharmX.dtos.SlotDto;
import com.alaia.pharmX.dtos.SlotPatchDto;
import com.alaia.pharmX.exceptions.servicesImpl.SectionNotFoundException;
import com.alaia.pharmX.exceptions.servicesImpl.SlotAlreadyExistsException;
import com.alaia.pharmX.exceptions.servicesImpl.SlotNotFoundException;
import com.alaia.pharmX.mappers.SlotMapper;
import com.alaia.pharmX.models.Section;
import com.alaia.pharmX.models.Slot;
import com.alaia.pharmX.repositories.SectionRepository;
import com.alaia.pharmX.repositories.SlotRepository;
import com.alaia.pharmX.services.SlotService;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SlotServiceImp implements SlotService{

	@Autowired
	private SlotRepository slotRepository;

	@Autowired
    private SectionRepository sectionRepository;

	@Autowired
    private SlotMapper slotMapper;

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public SlotDto create(SlotDto dto, Long sectionId) {

		if (slotRepository.existsByCode(dto.getCode())) {
			throw new SlotAlreadyExistsException("Slot already exists with code: " + dto.getCode());
		}

		Slot slot = slotMapper.toEntity(dto);

        if (sectionId != null) {
        	Section section = sectionRepository.findById(sectionId)
                    .orElseThrow(() -> new SectionNotFoundException("Section not found with ID : " + sectionId));
            slot.setSection(section);
            section.getSlots().add(slot);
        }

        Slot saved = slotRepository.save(slot);
        return slotMapper.toDto(saved);
    }

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public SlotDto getById(long id) {

		Slot slot = slotRepository.findById(id)
                .orElseThrow(() -> new SlotNotFoundException("Slot not found with ID : " + id));
        return slotMapper.toDto(slot);
	}

	@Override
	public SlotDto getByCode(String code) {

		Slot slot = slotRepository.findByCode(code)
        		.orElseThrow(() -> new SlotNotFoundException("Slot not found with code : " + code));
        return slotMapper.toDto(slot);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public List<SlotDto> getAllSlots() {

		List<Slot> slots = slotRepository.findAll();
		return slots.stream()
				.map(slotMapper::toDto)
				.toList();
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public SlotDto patchSlot(long id, SlotPatchDto dto) {

		Slot slot = slotRepository.findById(id)
	            .orElseThrow(() -> new SlotNotFoundException("Slot not found with ID : " + id));

	    if (dto.getVolume() != null) {
	        slot.setVolume(dto.getVolume());
	    }
	    if (dto.getPickingSequence() != null) {
	        slot.setPickingSequence(dto.getPickingSequence());
	    }

	    return slotMapper.toDto(slot);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public SlotDto delete(long id) {

		Slot slot = slotRepository.findById(id)
				.orElseThrow(() -> new SlotNotFoundException("Slot not found with ID : " + id));
		Section parent = slot.getSection();
		if (parent != null) {
			parent.getSlots().remove(slot);
			slot.setSection(null);
		}
		slotRepository.delete(slot);
		return slotMapper.toDto(slot);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public SlotDto move(long slotId, long targetSectionId) {

		Section target = sectionRepository.findById(targetSectionId)
                .orElseThrow(() -> new SectionNotFoundException("Section not found with ID : " + targetSectionId));
        Slot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new SlotNotFoundException("Slot not found with ID : " + slotId));

        slot.setSection(target);

        slotRepository.save(slot);

        return slotMapper.toDto(slot);
	}
}