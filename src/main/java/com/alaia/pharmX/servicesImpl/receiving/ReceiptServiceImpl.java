package com.alaia.pharmX.servicesImpl.receiving;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alaia.pharmX.dtos.receiving.CreateLotRequest;
import com.alaia.pharmX.dtos.receiving.CreateReceiptLineRequest;
import com.alaia.pharmX.dtos.receiving.CreateReceiptRequest;
import com.alaia.pharmX.dtos.receiving.ReceiptDto;
import com.alaia.pharmX.dtos.receiving.SlotDefiniedAtPost;
import com.alaia.pharmX.dtos.receiving.UpdateReceiptLineRequest;
import com.alaia.pharmX.dtos.receiving.VerifyReceiptLineRequest;
import com.alaia.pharmX.dtos.receiving.putAwayReceiptResponse;
import com.alaia.pharmX.dtos.stock.StockOperation;
import com.alaia.pharmX.exceptions.servicesImpl.InvalidProductConfigurationException;
import com.alaia.pharmX.exceptions.servicesImpl.InvalidSlotConfigurationException;
import com.alaia.pharmX.exceptions.servicesImpl.InvalidStateTransitionException;
import com.alaia.pharmX.exceptions.servicesImpl.LotAlreadyExistsException;
import com.alaia.pharmX.exceptions.servicesImpl.NoMatchCategoryException;
import com.alaia.pharmX.exceptions.servicesImpl.NonCompliantQuantityException;
import com.alaia.pharmX.exceptions.servicesImpl.ProductNotFoundException;
import com.alaia.pharmX.exceptions.servicesImpl.ReceiptLineNotFoundToReceiptException;
import com.alaia.pharmX.exceptions.servicesImpl.ReceiptNotFoundException;
import com.alaia.pharmX.exceptions.servicesImpl.SlotNotFoundException;
import com.alaia.pharmX.mappers.receiving.ReceiptMapper;
import com.alaia.pharmX.models.Product;
import com.alaia.pharmX.models.Section;
import com.alaia.pharmX.models.Slot;
import com.alaia.pharmX.models.receiving.Lot;
import com.alaia.pharmX.models.receiving.MovementType;
import com.alaia.pharmX.models.receiving.Receipt;
import com.alaia.pharmX.models.receiving.ReceiptLine;
import com.alaia.pharmX.models.receiving.ReceiptLineStatus;
import com.alaia.pharmX.models.receiving.ReceiptState;
import com.alaia.pharmX.repositories.ProductRepository;
import com.alaia.pharmX.repositories.SectionRepository;
import com.alaia.pharmX.repositories.SlotRepository;
import com.alaia.pharmX.repositories.receiving.LotRepository;
import com.alaia.pharmX.repositories.receiving.ReceiptLineRepository;
import com.alaia.pharmX.repositories.receiving.ReceiptRepository;
import com.alaia.pharmX.services.receiving.ReceiptService;
import com.alaia.pharmX.services.stock.StockService;

import org.springframework.transaction.annotation.Transactional;

@Service
public class ReceiptServiceImpl implements ReceiptService{

	@Autowired
	private ReceiptRepository receiptRepository;

	@Autowired
	private LotRepository lotRepository;

	@Autowired
	private SlotRepository slotRepository;

	@Autowired
	private ReceiptLineRepository receiptLineRepository;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private SectionRepository sectionRepository;

	@Autowired
	private ReceiptMapper receiptMapper;

	@Autowired
	private StockService stockService;

	@Override
	@Transactional
	public ReceiptDto createDraft(CreateReceiptRequest request) {
	    Receipt receipt = initializeReceipt(request);
	    addLinesToReceipt(receipt, request.getLines());
	    return saveAndMapReceipt(receipt);
	}

	@Override
    @Transactional
    public ReceiptDto addLine(Long receiptId, CreateReceiptLineRequest line) {
        Receipt receipt = validateReceiptAndState(receiptId, ReceiptState.DRAFT);
        Optional<ReceiptLine> existingLine = receiptLineRepository.findLineByNationalCodeAndReceipt(line.getNationalCode(), receipt);
        ReceiptLine receiptLine;
        if (existingLine.isPresent()) {
            receiptLine = existingLine.get();
            receiptLine.setQtyExpected(receiptLine.getQtyExpected() + line.getQtyExpected());
        } else {
            receiptLine = validateAndCreateReceiptLine(line, receipt);
            receipt.getLines().add(receiptLine);
        }
        return saveAndMapReceipt(receipt);
    }

	@Override
    @Transactional
    public ReceiptDto updateLine(Long receiptId, Long lineId, UpdateReceiptLineRequest line) {
        Receipt receipt = validateReceiptAndState(receiptId, ReceiptState.DRAFT);
        ReceiptLine receiptLine = findLine(receipt, lineId);
        updateReceiptLine(receiptLine, line);
        return saveAndMapReceipt(receipt);
    }

	@Override
    public ReceiptDto deleteLineToReceipt(Long receiptId, Long lineId) {
        Receipt receipt = validateReceiptAndState(receiptId, ReceiptState.DRAFT);
        ReceiptLine receiptLine = findLine(receipt, lineId);
        receipt.getLines().remove(receiptLine);
        updateReceiptLastModification(receipt);
        receiptLineRepository.delete(receiptLine);
        return receiptMapper.toDto(receipt);
	}

	@Override
	@Transactional
	public ReceiptDto verifyLine(Long receiptId, Long lineId, VerifyReceiptLineRequest request) {
		Receipt receipt = validateReceiptAndState(receiptId, ReceiptState.DRAFT, ReceiptState.VERIFIED);
		ReceiptLine receiptLine = findLine(receipt, lineId);
		updateReceiptLineWithVerification(receiptLine, request);
		assignPutawaySlotIfProvided(receiptLine, request.getPutwaySlotCode());
		manageLotsForReceiptLine(receiptLine, request);
		updateReceiptStateIfVerified(receipt);
		receiptLineRepository.saveAndFlush(receiptLine);
		return receiptMapper.toDto(receipt);
	}

	@Override
    @Transactional
    public putAwayReceiptResponse putAwayReceipt(Long receiptId, List<SlotDefiniedAtPost> listSlotAtPost) {
        Receipt receipt = validateReceiptAndState(receiptId, ReceiptState.VERIFIED);
        assignSlotsForPost(receipt, listSlotAtPost);
        validateAllLinesHaveSlots(receipt);
        int createdMovements = createStockOperationsForReceipt(receipt);
        receipt.setState(ReceiptState.PUTAWAY);
        updateReceiptLastModification(receipt);
        return buildPutAwayReceiptResponse(receiptId, createdMovements, LocalDateTime.now());
    }

	@Override
    @Transactional
    public ReceiptDto cancel(Long receiptId) {
        Receipt receipt = checkExistingReceipt(receiptId);
        if (receipt.getState() == ReceiptState.PUTAWAY) {
            throw new InvalidStateTransitionException("Cannot cancel a POSTED receipt");
        }
        receipt.setState(ReceiptState.CANCELED);
        return saveAndMapReceipt(receipt);
    }

	@Override
    public ReceiptDto getReceiptById(Long receiptId) {
        Receipt receipt = checkExistingReceipt(receiptId);
        return receiptMapper.toDto(receipt);
    }

    @Override
    public List<ReceiptDto> listReceipt() {
        return receiptRepository.findAll().stream()
                .map(receiptMapper::toDto)
                .toList();
    }

    @Override
    public ReceiptDto deleteReceipt(Long receiptId) {
        Receipt receipt = checkExistingReceipt(receiptId);
        receiptRepository.delete(receipt);
        return receiptMapper.toDto(receipt);
    }

    //-----------> HELPERS <-----------

    private Receipt initializeReceipt(CreateReceiptRequest request) {
        Receipt receipt = new Receipt();
        receipt.setExternalRef(request.getExternalRef());
        receipt.setSupplierName(request.getSupplierName());
        receipt.setReceivedAt(LocalDateTime.now());
        receipt.setLastModification(LocalDateTime.now());
        receipt.setState(ReceiptState.DRAFT);
        receipt.setLines(new HashSet<>());
        return receipt;
    }

    private void addLinesToReceipt(Receipt receipt, Set<CreateReceiptLineRequest> lines) {
        if (lines != null) {
            for (CreateReceiptLineRequest line : lines) {
                ReceiptLine receiptLine = validateAndCreateReceiptLine(line, receipt);
                receipt.getLines().add(receiptLine);
            }
        }
    }

    private ReceiptLine validateAndCreateReceiptLine(CreateReceiptLineRequest line, Receipt receipt) {
        if (!productRepository.existsByNationalCode(line.getNationalCode())) {
            throw new ProductNotFoundException("Product not found with nationalCode: " + line.getNationalCode());
        }
        ReceiptLine receiptLine = new ReceiptLine();
        receiptLine.setReceipt(receipt);
        receiptLine.setNationalCode(line.getNationalCode());
        receiptLine.setQtyExpected(line.getQtyExpected());
        return receiptLine;
    }

    private void updateReceiptLine(ReceiptLine receiptLine, UpdateReceiptLineRequest line) {
    	if (line.getQtyExpected() != null) {
    		receiptLine.setQtyExpected(line.getQtyExpected());
    	}
    }

    private void updateReceiptLineWithVerification(ReceiptLine receiptLine, VerifyReceiptLineRequest request) {
    	receiptLine.setQtyReceived(request.getQtyReceived());
    	receiptLine.setStatus(request.getStatus());
    	receiptLine.setReason(request.getReason());
    }

    private void assignPutawaySlotIfProvided(ReceiptLine receiptLine, String slotCode) {
    	if (slotCode != null) {
    		Slot slot = resolveAndValidatePutawaySlot(slotCode, receiptLine.getNationalCode());
    		receiptLine.setPutawaySlot(slot);
    	}
    }

    private void manageLotsForReceiptLine(ReceiptLine receiptLine, VerifyReceiptLineRequest request) {
    	receiptLine.getLots().clear();
    	if (request.getQtyReceived() <= 0) {
    		return;
    	}
    	if (request.getLots() == null || request.getLots().isEmpty()) {
    		createDefaultLot(receiptLine, request.getQtyReceived(), request.getReason());
    	} else {
    		validateLotQuantities(request.getLots(), request.getQtyReceived());
    		createLotsFromRequest(receiptLine, request.getLots());
    	}
    }

    private void createDefaultLot(ReceiptLine receiptLine, Integer qtyReceived, String reason) {
    	Lot lot = new Lot();
    	lot.setReceiptLine(receiptLine);
    	lot.setQuantity(qtyReceived);
    	lot.setNotes(reason);
    	lot.setLotCode(generateLotCode(receiptLine));
    	receiptLine.getLots().add(lot);
    }

    private void validateLotQuantities(List<CreateLotRequest> lots, Integer qtyReceived) {
    	int sum = lots.stream()
    			.mapToInt(lr -> lr.getQuantity() != null ? lr.getQuantity() : 0)
    			.sum();
    	if (sum != qtyReceived) {
    		throw new NonCompliantQuantityException("Sum of lot quantities must equal qtyReceived");
    	}
    	for (CreateLotRequest lr : lots) {
    		if (lr.getLotCode() != null && lotRepository.existsByLotCode(lr.getLotCode())) {
    			throw new LotAlreadyExistsException("Lot code already exists: " + lr.getLotCode());
    		}
    	}
    }

    private void createLotsFromRequest(ReceiptLine receiptLine, List<CreateLotRequest> lots) {
    	for (CreateLotRequest lr : lots) {
    		Lot lot = new Lot();
    		lot.setReceiptLine(receiptLine);
    		lot.setQuantity(lr.getQuantity());
    		lot.setExpiryDate(lr.getExpiryDate());
    		lot.setNotes(lr.getNotes());
    		lot.setLotCode(lr.getLotCode() != null ? lr.getLotCode() : generateLotCode(receiptLine));
    		receiptLine.getLots().add(lot);
    	}
    }

    private void assignSlotsForPost(Receipt receipt, List<SlotDefiniedAtPost> listSlotAtPost) {
    	if (!listSlotAtPost.isEmpty()) {
    		for (ReceiptLine rl : receipt.getLines()) {
    			if (rl.getPutawaySlot() == null) {
    				int index = findIntoList(rl.getNationalCode(), listSlotAtPost);
    				if (index != -1) {
    					SlotDefiniedAtPost s = listSlotAtPost.get(index);
    					Slot slot = resolveAndValidatePutawaySlot(s.getSlotCode(), rl.getNationalCode());
    					rl.setPutawaySlot(slot);
    				} else {
    					resolveValidatePutawaySlot(rl);
    				}
    			}
    		}
    	} else {
    		for (ReceiptLine rl : receipt.getLines()) {
    			if (rl.getPutawaySlot() == null) {
    				resolveValidatePutawaySlot(rl);
    			}
    		}
    	}
    }

    private void validateAllLinesHaveSlots(Receipt receipt) {
    	for (ReceiptLine rl : receipt.getLines()) {
    		if (rl.getPutawaySlot() == null) {
    			throw new InvalidStateTransitionException("Receipt: " + receipt.getId() + " cannot be posted. ReceiptLine: " + rl.getId() + " has putawaySlot equals NULL");
    		}
    	}
    }

    private int createStockOperationsForReceipt(Receipt receipt) {
    	int created = 0;
    	for (ReceiptLine rl : receipt.getLines()) {
    		int qty = rl.getQtyReceived() != null ? rl.getQtyReceived() : 0;
    		if (qty <= 0 || rl.getStatus() == ReceiptLineStatus.DAMAGED) {
    			continue;
    		}
    		StockOperation operation = buildStockOperation(rl, MovementType.INBOUND_RECEIPT, qty, receipt.getId());
    		stockService.onReceiptOpration(operation);
    		created++;
    	}
    	return created;
    }

    private putAwayReceiptResponse buildPutAwayReceiptResponse(Long receiptId, int createdMovements, LocalDateTime postedAt) {
    	putAwayReceiptResponse response = new putAwayReceiptResponse();
    	response.setReceiptId(receiptId);
    	response.setMovementCount(createdMovements);
    	response.setPostedAt(postedAt);
    	return response;
    }

    private Receipt validateReceiptAndState(Long receiptId, ReceiptState... allowedStates) {
    	Receipt receipt = checkExistingReceipt(receiptId);
    	restrictState(receipt, allowedStates);
    	return receipt;
    }

    private void restrictState(Receipt receipt, ReceiptState... allowedStates) {
    	for (ReceiptState state : allowedStates) {
    		if (receipt.getState() == state) {
    			return;
    		}
    	}
    	String allowed = String.join(" or ", Arrays.stream(allowedStates).map(Enum::name).toList());
    	throw new InvalidStateTransitionException("Receipt must be " + allowed + " but is " + receipt.getState());
    }

    private ReceiptLine findLine(Receipt receipt, Long lineId) {
    	return receipt.getLines().stream()
    			.filter(l -> Objects.equals(l.getId(), lineId))
    			.findFirst()
    			.orElseThrow(() -> new ReceiptLineNotFoundToReceiptException("Line " + lineId + " not found on receipt " + receipt.getId()));
    }

    private boolean allLinesVerified(Receipt receipt) {
    	if (receipt.getLines() == null || receipt.getLines().isEmpty()) {
    		return false;
    	}
    	for (ReceiptLine rl : receipt.getLines()) {
    		if (rl.getQtyReceived() == null || rl.getStatus() == null) {
    			return false;
    		}
    		if (rl.getQtyReceived() > 0 && (rl.getLots() == null || rl.getLots().isEmpty())) {
    			return false;
    		}
    		int sumLots = rl.getLots().stream().mapToInt(l -> l.getQuantity() != null ? l.getQuantity() : 0).sum();
    		if (rl.getQtyReceived() > 0 && sumLots != rl.getQtyReceived()) {
    			return false;
    		}
    	}
    	return true;
    }

    private void updateReceiptStateIfVerified(Receipt receipt) {
    	if (allLinesVerified(receipt)) {
    		receipt.setState(ReceiptState.VERIFIED);
    		updateReceiptLastModification(receipt);
    	}
    }

    private void updateReceiptLastModification(Receipt receipt) {
		receipt.setLastModification(LocalDateTime.now());
	}

	private String generateLotCode(ReceiptLine receiptLine) {
    	String base = "L" + java.time.LocalDate.now().toString().replace("-", "");
    	String rcpt = String.valueOf(receiptLine.getReceipt() != null ? receiptLine.getReceipt().getId() : 0);
    	String line = String.valueOf(receiptLine.getId());
    	String rand = java.util.UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    	return base + "-" + rcpt + "-" + line + "-" + rand;
    }

    private int findIntoList(String nationalCode, List<SlotDefiniedAtPost> listSlotAtPost) {
    	int index = 0;
    	for (SlotDefiniedAtPost s : listSlotAtPost) {
    		if (s.getNationalCode().equals(nationalCode)) {
    			return index;
    		}
    		index++;
    	}
    	return -1;
    }

    private Slot resolveAndValidatePutawaySlot(String slotCode, String productNationalCode) {
    	Slot slot = slotRepository.findByCode(slotCode)
    			.orElseThrow(() -> new SlotNotFoundException("Slot: " + slotCode + " not found"));

    	Product product = productRepository.findByNationalCode(productNationalCode);
    	if (product == null) {
    		throw new ProductNotFoundException("Product " + productNationalCode + " not found");
    	}

    	if (product.getCategory() == null) {
    		throw new InvalidProductConfigurationException(
    				"Product " + productNationalCode + " has no category configured");
    	}

    	Section section = slot.getSection();
    	if (section == null || section.getCategory() == null) {
    		throw new InvalidSlotConfigurationException(
    				"Slot: " + slotCode + " has no section/category configured");
    	}

    	if (!product.getCategory().equals(section.getCategory())) {
    		throw new NoMatchCategoryException(
    				"Product: " + productNationalCode + ", category: " + product.getCategory()
    				+ " is different to Slot: " + slotCode + ", category: " + section.getCategory());
    	}

    	return slot;
    }

    private void resolveValidatePutawaySlot(ReceiptLine receiptLine) {
    	Product product = productRepository.findByNationalCode(receiptLine.getNationalCode());
    	Section section = sectionRepository.findByCategory(product.getCategory());
    	Set<Slot> slots = section.getSlots();
    	if (slots != null && !slots.isEmpty()) {
    		Slot first = slots.iterator().next();
    		receiptLine.setPutawaySlot(first);
    	} else {
    		throw new SlotNotFoundException("Slots not found for category: " + section.getCategory());
    	}
    }

    private Receipt checkExistingReceipt(Long receiptId) {
    	return receiptRepository.findById(receiptId)
    			.orElseThrow(() -> new ReceiptNotFoundException("Receipt " + receiptId + " not found"));
    }

    private ReceiptDto saveAndMapReceipt(Receipt receipt) {
    	updateReceiptLastModification(receipt);
    	Receipt savedReceipt = receiptRepository.saveAndFlush(receipt);
        return receiptMapper.toDto(savedReceipt);
    }

    private StockOperation buildStockOperation(ReceiptLine receiptLine, MovementType type, Integer quantity, Long referenceId) {
    	StockOperation operation = new StockOperation();
    	operation.setNationalCode(receiptLine.getNationalCode());
    	operation.setReferenceType("RECEIPT");
    	operation.setReferenceId(referenceId);
    	operation.setType(type);
    	operation.setSlot(receiptLine.getPutawaySlot());
    	operation.setQuantity(quantity);
    	return operation;
    }
}