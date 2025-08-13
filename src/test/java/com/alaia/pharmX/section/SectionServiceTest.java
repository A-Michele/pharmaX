package com.alaia.pharmX.section;

import java.util.Collections;
import java.util.HashSet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.alaia.pharmX.dtos.SectionDto;
import com.alaia.pharmX.dtos.SectionUpdateDto;
import com.alaia.pharmX.dtos.SlotDto;
import com.alaia.pharmX.mappers.SectionMapper;
import com.alaia.pharmX.mappers.SlotMapper;
import com.alaia.pharmX.models.Section;
import com.alaia.pharmX.models.Slot;
import com.alaia.pharmX.repositories.SectionRepository;
import com.alaia.pharmX.repositories.SlotRepository;
import com.alaia.pharmX.servicesImpl.SectionServiceImp;
import com.alaia.pharmX.servicesImpl.exceptions.SectionAlreadyExistsException;
import com.alaia.pharmX.servicesImpl.exceptions.SectionNotFoundException;
import com.alaia.pharmX.servicesImpl.exceptions.SlotNotFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
class SectionServiceTest {

    @Mock
    private SectionRepository sectionRepository;

    @Mock
    private SlotRepository slotRepository;

    @Mock
    private SectionMapper sectionMapper;

    @Mock
    private SlotMapper slotMapper;

    @InjectMocks
    private SectionServiceImp service;

    private Section section;
    private SectionDto sectionDto;
    private Slot slot;
    private SlotDto slotDto;

    @BeforeEach
    void setUp() {
        section = new Section();
        section.setId(1);
        section.setCode("SEC-001");
        section.setName("Section Name");
        section.setCategory("Category");
        section.setSlots(new HashSet<>());

        sectionDto = new SectionDto(1, "SEC-001", "Section Name", "Category", new HashSet<>());

        slot = new Slot();
        slot.setId(10L);
        slot.setCode("SLOT-001");
        slot.setVolume(100);
        slot.setPickingSequence("A1");

        slotDto = new SlotDto(10L, "SLOT-001", 100, "A1");
    }

    // -----------> CREATE <-----------

    @Test
    void create_SectionAlreadyExists_ShouldThrow() {
        when(sectionRepository.existsByCode("SEC-001")).thenReturn(true);

        assertThrows(SectionAlreadyExistsException.class, () -> service.create(sectionDto));

        verify(sectionRepository).existsByCode("SEC-001");
        verifyNoMoreInteractions(sectionRepository);
        verifyNoInteractions(sectionMapper, slotRepository);
    }

    @Test
    void create_WithSlots_ShouldSaveSection() {
        sectionDto.setSlots(Set.of(slotDto));
        Section mapped = new Section();
        mapped.setCode("SEC-001");
        mapped.setSlots(Set.of(slot));

        when(sectionRepository.existsByCode("SEC-001")).thenReturn(false);
        when(sectionMapper.toEntity(sectionDto)).thenReturn(mapped);
        when(sectionRepository.save(any(Section.class))).thenReturn(section);
        when(sectionMapper.toDto(section)).thenReturn(sectionDto);

        SectionDto result = service.create(sectionDto);

        assertEquals(sectionDto, result);
        verify(sectionRepository).save(any(Section.class));
    }

    @Test
    void create_WithoutSlots_ShouldSaveEmptySet() {
        sectionDto.setSlots(null);
        Section mapped = new Section();
        mapped.setCode("SEC-001");

        when(sectionRepository.existsByCode("SEC-001")).thenReturn(false);
        when(sectionMapper.toEntity(sectionDto)).thenReturn(mapped);
        when(sectionRepository.save(any(Section.class))).thenReturn(section);
        when(sectionMapper.toDto(section)).thenReturn(sectionDto);

        SectionDto result = service.create(sectionDto);

        assertEquals(sectionDto, result);
        assertTrue(mapped.getSlots().isEmpty());
    }

    // -----------> GET BY ID <-----------

    @Test
    void getById_SectionFound_ShouldReturnDto() {
        when(sectionRepository.findById(1L)).thenReturn(Optional.of(section));
        when(sectionMapper.toDto(section)).thenReturn(sectionDto);

        SectionDto result = service.getById(1L);

        assertEquals(sectionDto, result);
    }

    @Test
    void getById_SectionNotFound_ShouldThrow() {
        when(sectionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(SectionNotFoundException.class, () -> service.getById(1L));
    }

    // -----------> GET BY CODE <-----------

    @Test
    void getByCode_SectionFound_ShouldReturnDto() {
        when(sectionRepository.findByCode("SEC-001")).thenReturn(Optional.of(section));
        when(sectionMapper.toDto(section)).thenReturn(sectionDto);

        SectionDto result = service.getByCode("SEC-001");

        assertEquals(sectionDto, result);
    }

    @Test
    void getByCode_SectionNotFound_ShouldThrow() {
        when(sectionRepository.findByCode("SEC-001")).thenReturn(Optional.empty());

        assertThrows(SectionNotFoundException.class, () -> service.getByCode("SEC-001"));
    }

    // -----------> GET ALL <-----------

    @Test
    void getAllSection_ShouldReturnList() {
        when(sectionRepository.findAll()).thenReturn(List.of(section));
        when(sectionMapper.toDto(section)).thenReturn(sectionDto);

        List<SectionDto> result = service.getAllSection();

        assertEquals(1, result.size());
        assertEquals(sectionDto, result.get(0));
    }

    @Test
    void getAllSection_EmptyList_ShouldReturnEmpty() {
        when(sectionRepository.findAll()).thenReturn(Collections.emptyList());

        List<SectionDto> result = service.getAllSection();

        assertTrue(result.isEmpty());
    }

    // -----------> UPDATE NAME & CATEGORY <-----------

    @Test
    void updateNameAndCategory_SectionFound_ShouldUpdate() {
        SectionUpdateDto updateDto = new SectionUpdateDto();
        updateDto.setName("New Name");
        updateDto.setCategory("New Cat");

        when(sectionRepository.findById(1L)).thenReturn(Optional.of(section));
        when(sectionMapper.toDto(section)).thenReturn(sectionDto);

        SectionDto result = service.updateNameAndCategory(1L, updateDto);

        assertEquals(sectionDto, result);
        assertEquals("New Name", section.getName());
        assertEquals("New Cat", section.getCategory());
    }

    @Test
    void updateNameAndCategory_SectionNotFound_ShouldThrow() {
        when(sectionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(SectionNotFoundException.class,
                () -> service.updateNameAndCategory(1L, new SectionUpdateDto()));
    }

    // -----------> DELETE <-----------

    @Test
    void delete_SectionFound_ShouldDelete() {
        when(sectionRepository.findById(1L)).thenReturn(Optional.of(section));
        when(sectionMapper.toDto(section)).thenReturn(sectionDto);

        SectionDto result = service.delete(1L);

        assertEquals(sectionDto, result);
        verify(sectionRepository).delete(section);
    }

    @Test
    void delete_SectionNotFound_ShouldThrow() {
        when(sectionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(SectionNotFoundException.class, () -> service.delete(1L));
    }

    // -----------> ADD EXISTING SLOT <-----------

    @Test
    void addExistingSlot_AlreadyInSameSection_ShouldThrow() {
        section.getSlots().add(slot);
        slot.setSection(section);

        when(sectionRepository.findById(1L)).thenReturn(Optional.of(section));
        when(slotRepository.findById(10L)).thenReturn(Optional.of(slot));

        assertThrows(IllegalStateException.class, () -> service.addExistingSlot(1L, 10L));
    }

    @Test
    void addExistingSlot_SectionNotFound_ShouldThrow() {
        when(sectionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(SectionNotFoundException.class, () -> service.addExistingSlot(1L, 10L));
    }

    @Test
    void addExistingSlot_SlotNotFound_ShouldThrow() {
        when(sectionRepository.findById(1L)).thenReturn(Optional.of(section));
        when(slotRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(SlotNotFoundException.class, () -> service.addExistingSlot(1L, 10L));
    }

    @Test
    void addExistingSlot_Success_ShouldAddSlot() {
        when(sectionRepository.findById(1L)).thenReturn(Optional.of(section));
        when(slotRepository.findById(10L)).thenReturn(Optional.of(slot));
        when(sectionMapper.toDto(section)).thenReturn(sectionDto);

        SectionDto result = service.addExistingSlot(1L, 10L);

        assertEquals(sectionDto, result);
        assertEquals(section, slot.getSection());
        verify(slotRepository).saveAndFlush(slot);
    }

    // -----------> REMOVE SLOT <-----------

    @Test
    void removeSlot_SlotNotFound_ShouldThrow() {
        when(slotRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(SlotNotFoundException.class, () -> service.removeSlot(1L, 10L));
    }

    @Test
    void removeSlot_SlotNotInSection_ShouldThrow() {
        Slot otherSlot = new Slot();
        otherSlot.setId(11L);
        otherSlot.setSection(new Section()); // id diverso
        otherSlot.getSection().setId(99);

        when(slotRepository.findById(11L)).thenReturn(Optional.of(otherSlot));

        assertThrows(IllegalStateException.class, () -> service.removeSlot(1L, 11L));
    }

    @Test
    void removeSlot_Success_ShouldRemoveSlot() {
        slot.setSection(section);

        when(slotRepository.findById(10L)).thenReturn(Optional.of(slot));
        when(sectionRepository.getReferenceById(1L)).thenReturn(section);
        when(sectionMapper.toDto(section)).thenReturn(sectionDto);

        SectionDto result = service.removeSlot(1L, 10L);

        assertEquals(sectionDto, result);
        assertNull(slot.getSection());
        verify(slotRepository).saveAndFlush(slot);
    }
}
