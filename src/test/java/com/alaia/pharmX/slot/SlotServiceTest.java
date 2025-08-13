package com.alaia.pharmX.slot;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.alaia.pharmX.dtos.SlotDto;
import com.alaia.pharmX.dtos.SlotPatchDto;
import com.alaia.pharmX.mappers.SlotMapper;
import com.alaia.pharmX.models.Section;
import com.alaia.pharmX.models.Slot;
import com.alaia.pharmX.repositories.SectionRepository;
import com.alaia.pharmX.repositories.SlotRepository;
import com.alaia.pharmX.servicesImpl.SlotServiceImp;
import com.alaia.pharmX.servicesImpl.exceptions.SectionNotFoundException;
import com.alaia.pharmX.servicesImpl.exceptions.SlotAlreadyExistsException;
import com.alaia.pharmX.servicesImpl.exceptions.SlotNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

@ExtendWith(MockitoExtension.class)
class SlotServiceTest {

    @Mock
    private SlotRepository slotRepository;

    @Mock
    private SectionRepository sectionRepository;

    @Mock
    private SlotMapper slotMapper;

    @InjectMocks
    private SlotServiceImp service;

    private Slot slot;
    private SlotDto slotDto;
    private Section section;

    @BeforeEach
    void setUp() {
        slot = new Slot();
        slot.setId(10L);
        slot.setCode("SLOT-001");
        slot.setVolume(100);
        slot.setPickingSequence("A1");

        slotDto = new SlotDto(10L, "SLOT-001", 100, "A1");

        section = new Section();
        section.setId(1);
        section.setCode("SEC-001");
        section.setName("Name");
        section.setCategory("Cat");
        section.setSlots(new HashSet<>());
    }

    // -----------> CREATE <-----------

    @Test
    void create_SlotAlreadyExists_ShouldThrow() {
        // given
        when(slotRepository.existsByCode("SLOT-001")).thenReturn(true);

        // when-then
        assertThrows(SlotAlreadyExistsException.class, () -> service.create(slotDto, null));

        // verify
        verify(slotRepository).existsByCode("SLOT-001");
        verifyNoMoreInteractions(slotRepository);
        verifyNoInteractions(slotMapper, sectionRepository);
    }

    @Test
    void create_WithoutSection_ShouldPersist() {
        // given
        when(slotRepository.existsByCode("SLOT-001")).thenReturn(false);

        Slot mapped = new Slot();
        mapped.setCode("SLOT-001");
        mapped.setVolume(100);
        mapped.setPickingSequence("A1");

        when(slotMapper.toEntity(slotDto)).thenReturn(mapped);

        Slot saved = new Slot();
        saved.setId(10L);
        saved.setCode("SLOT-001");
        saved.setVolume(100);
        saved.setPickingSequence("A1");

        when(slotRepository.save(mapped)).thenReturn(saved);
        when(slotMapper.toDto(saved)).thenReturn(slotDto);

        // when
        SlotDto result = service.create(slotDto, null);

        // then
        assertEquals(slotDto, result);
        verify(slotRepository).save(mapped);
    }

    @Test
    void create_WithSectionNotFound_ShouldThrow() {
        // given
        when(slotRepository.existsByCode("SLOT-001")).thenReturn(false);
        when(slotMapper.toEntity(slotDto)).thenReturn(new Slot());
        when(sectionRepository.findById(1L)).thenReturn(Optional.empty());

        // when-then
        assertThrows(SectionNotFoundException.class, () -> service.create(slotDto, 1L));
    }

    @Test
    void create_WithSection_ShouldAssignAndPersist() {
        // given
        when(slotRepository.existsByCode("SLOT-001")).thenReturn(false);

        Slot mapped = new Slot();
        mapped.setCode("SLOT-001");

        when(slotMapper.toEntity(slotDto)).thenReturn(mapped);
        when(sectionRepository.findById(1L)).thenReturn(Optional.of(section));

        Slot saved = new Slot();
        saved.setId(10L);
        saved.setCode("SLOT-001");
        saved.setSection(section);

        when(slotRepository.save(mapped)).thenReturn(saved);
        when(slotMapper.toDto(saved)).thenReturn(slotDto);

        // when
        SlotDto result = service.create(slotDto, 1L);

        // then
        assertEquals(slotDto, result);
        assertEquals(section, mapped.getSection());
        assertTrue(section.getSlots().contains(mapped));
        verify(slotRepository).save(mapped);
    }

    // -----------> GET BY ID <-----------

    @Test
    void getById_Found_ShouldReturnDto() {
        // given
        when(slotRepository.findById(10L)).thenReturn(Optional.of(slot));
        when(slotMapper.toDto(slot)).thenReturn(slotDto);

        // when
        SlotDto result = service.getById(10L);

        // then
        assertEquals(slotDto, result);
    }

    @Test
    void getById_NotFound_ShouldThrow() {
        // given
        when(slotRepository.findById(10L)).thenReturn(Optional.empty());

        // when-then
        assertThrows(SlotNotFoundException.class, () -> service.getById(10L));
    }

    // -----------> GET BY CODE <-----------

    @Test
    void getByCode_Found_ShouldReturnDto() {
        // given
        when(slotRepository.findByCode("SLOT-001")).thenReturn(Optional.of(slot));
        when(slotMapper.toDto(slot)).thenReturn(slotDto);

        // when
        SlotDto result = service.getByCode("SLOT-001");

        // then
        assertEquals(slotDto, result);
    }

    @Test
    void getByCode_NotFound_ShouldThrow() {
        // given
        when(slotRepository.findByCode("SLOT-001")).thenReturn(Optional.empty());

        // when-then
        assertThrows(SlotNotFoundException.class, () -> service.getByCode("SLOT-001"));
    }

    // -----------> GET ALL <-----------

    @Test
    void getAllSlots_ShouldReturnMappedList() {
        // given
        Slot slot2 = new Slot();
        slot2.setId(11L);
        slot2.setCode("SLOT-002");
        SlotDto slotDto2 = new SlotDto(11L, "SLOT-002", 0, null);

        when(slotRepository.findAll()).thenReturn(List.of(slot, slot2));
        when(slotMapper.toDto(slot)).thenReturn(slotDto);
        when(slotMapper.toDto(slot2)).thenReturn(slotDto2);

        // when
        List<SlotDto> result = service.getAllSlots();

        // then
        assertEquals(2, result.size());
        assertEquals(slotDto, result.get(0));
        assertEquals(slotDto2, result.get(1));
    }

    @Test
    void getAllSlots_Empty_ShouldReturnEmpty() {
        // given
        when(slotRepository.findAll()).thenReturn(Collections.emptyList());

        // when
        List<SlotDto> result = service.getAllSlots();

        // then
        assertTrue(result.isEmpty());
        verify(slotRepository).findAll();
    }

    // -----------> PATCH (volume / pickingSequence) <-----------

    @Test
    void patchSlot_Found_UpdateBothFields() {
        // given
        SlotPatchDto patch = new SlotPatchDto();
        patch.setVolume(150);
        patch.setPickingSequence("B5");

        when(slotRepository.findById(10L)).thenReturn(Optional.of(slot));
        when(slotMapper.toDto(slot)).thenReturn(new SlotDto(10L, "SLOT-001", 150, "B5"));

        // when
        SlotDto result = service.patchSlot(10L, patch);

        // then
        assertEquals(150, slot.getVolume());
        assertEquals("B5", slot.getPickingSequence());
        assertEquals(150, result.getVolume());
        assertEquals("B5", result.getPickingSequence());
    }

    @Test
    void patchSlot_Found_NullFields_ShouldChangeNothing() {
        // given
        SlotPatchDto patch = new SlotPatchDto(); // tutti i campi null

        when(slotRepository.findById(10L)).thenReturn(Optional.of(slot));
        when(slotMapper.toDto(slot)).thenReturn(slotDto);

        // when
        SlotDto result = service.patchSlot(10L, patch);

        // then
        assertEquals(100, slot.getVolume());
        assertEquals("A1", slot.getPickingSequence());
        assertEquals(slotDto, result);
    }

    @Test
    void patchSlot_NotFound_ShouldThrow() {
        // given
        when(slotRepository.findById(10L)).thenReturn(Optional.empty());

        // when-then
        assertThrows(SlotNotFoundException.class, () -> service.patchSlot(10L, new SlotPatchDto()));
    }

    // -----------> DELETE <-----------

    @Test
    void delete_Found_NoParent_ShouldDelete() {
        // given
        when(slotRepository.findById(10L)).thenReturn(Optional.of(slot));
        when(slotMapper.toDto(slot)).thenReturn(slotDto);

        // when
        SlotDto result = service.delete(10L);

        // then
        assertEquals(slotDto, result);
        verify(slotRepository).delete(slot);
    }

    @Test
    void delete_Found_WithParent_ShouldDetachAndDelete() {
        // given
        section.getSlots().add(slot);
        slot.setSection(section);

        when(slotRepository.findById(10L)).thenReturn(Optional.of(slot));
        when(slotMapper.toDto(slot)).thenReturn(slotDto);

        // when
        SlotDto result = service.delete(10L);

        // then
        assertEquals(slotDto, result);
        assertNull(slot.getSection());
        assertFalse(section.getSlots().contains(slot));
        verify(slotRepository).delete(slot);
    }

    @Test
    void delete_NotFound_ShouldThrow() {
        // given
        when(slotRepository.findById(10L)).thenReturn(Optional.empty());

        // when-then
        assertThrows(SlotNotFoundException.class, () -> service.delete(10L));
    }

    // -----------> MOVE <-----------

    @Test
    void move_SectionNotFound_ShouldThrow() {
        // given
        when(sectionRepository.findById(99L)).thenReturn(Optional.empty());

        // when-then
        assertThrows(SectionNotFoundException.class, () -> service.move(10L, 99L));
    }

    @Test
    void move_SlotNotFound_ShouldThrow() {
        // given
        when(sectionRepository.findById(1L)).thenReturn(Optional.of(section));
        when(slotRepository.findById(10L)).thenReturn(Optional.empty());

        // when-then
        assertThrows(SlotNotFoundException.class, () -> service.move(10L, 1L));
    }

    @Test
    void move_Success_ShouldUpdateOwnerAndSave() {
        // given
        Section target = new Section();
        target.setId(2);
        target.setSlots(new HashSet<>());

        when(sectionRepository.findById(2L)).thenReturn(Optional.of(target));
        when(slotRepository.findById(10L)).thenReturn(Optional.of(slot));
        when(slotRepository.save(slot)).thenReturn(slot);
        when(slotMapper.toDto(slot)).thenReturn(slotDto);

        // when
        SlotDto result = service.move(10L, 2L);

        // then
        assertEquals(slotDto, result);
        assertEquals(target, slot.getSection());
        verify(slotRepository).save(slot);
    }
}

