package com.alaia.pharmX.servicesImpl.receiving;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.alaia.pharmX.dtos.receiving.CreateLotRequest;
import com.alaia.pharmX.dtos.receiving.CreateReceiptLineRequest;
import com.alaia.pharmX.dtos.receiving.CreateReceiptRequest;
import com.alaia.pharmX.dtos.receiving.PostReceiptResponse;
import com.alaia.pharmX.dtos.receiving.ReceiptDto;
import com.alaia.pharmX.dtos.receiving.SlotDefiniedAtPost;
import com.alaia.pharmX.dtos.receiving.UpdateReceiptLineRequest;
import com.alaia.pharmX.dtos.receiving.VerifyReceiptLineRequest;
import com.alaia.pharmX.exceptions.servicesImpl.InvalidProductConfigurationException;
import com.alaia.pharmX.exceptions.servicesImpl.InvalidSlotConfigurationException;
import com.alaia.pharmX.exceptions.servicesImpl.InvalidStateTransitionException;
import com.alaia.pharmX.exceptions.servicesImpl.LinesVerifiedException;
import com.alaia.pharmX.exceptions.servicesImpl.LotAlreadyExistsException;
import com.alaia.pharmX.exceptions.servicesImpl.NoMatchCategoryException;
import com.alaia.pharmX.exceptions.servicesImpl.NonCompliantQuantityException;
import com.alaia.pharmX.exceptions.servicesImpl.ProductNotFoundException;
import com.alaia.pharmX.exceptions.servicesImpl.QuantityMustBePositiveException;
import com.alaia.pharmX.exceptions.servicesImpl.ReceiptLineNotFoundToReceiptException;
import com.alaia.pharmX.exceptions.servicesImpl.ReceiptNotFoundException;
import com.alaia.pharmX.exceptions.servicesImpl.SlotNotFoundException;
import com.alaia.pharmX.mappers.receiving.ReceiptMapper;
import com.alaia.pharmX.models.Product;
import com.alaia.pharmX.models.Section;
import com.alaia.pharmX.models.Slot;
import com.alaia.pharmX.models.receiving.InventoryMovement;
import com.alaia.pharmX.models.receiving.Lot;
import com.alaia.pharmX.models.receiving.MovementType;
import com.alaia.pharmX.models.receiving.Receipt;
import com.alaia.pharmX.models.receiving.ReceiptLine;
import com.alaia.pharmX.models.receiving.ReceiptLineStatus;
import com.alaia.pharmX.models.receiving.ReceiptState;
import com.alaia.pharmX.repositories.ProductRepository;
import com.alaia.pharmX.repositories.SectionRepository;
import com.alaia.pharmX.repositories.SlotRepository;
import com.alaia.pharmX.repositories.receiving.InventoryMovementRepository;
import com.alaia.pharmX.repositories.receiving.LotRepository;
import com.alaia.pharmX.repositories.receiving.ReceiptLineRepository;
import com.alaia.pharmX.repositories.receiving.ReceiptRepository;
import com.alaia.pharmX.services.receiving.ReceiptService;

import jakarta.transaction.Transactional;

@Service
public class ReceiptServiceImpl implements ReceiptService{

	@Autowired
	private ReceiptRepository receiptRepository;

	@Autowired
	private InventoryMovementRepository movementRepository;

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

	@Override
	@Transactional
	public ReceiptDto createDraft(CreateReceiptRequest request) {

		Receipt receipt = new Receipt();
		receipt.setExternalRef(request.getExternalRef());
		receipt.setSupplierName(request.getSupplierName());
		receipt.setReceivedAt(null);
		receipt.setState(ReceiptState.DRAFT);
		receipt.setLines(new HashSet<>());

		if(request.getLines() != null) {
			for(CreateReceiptLineRequest lr : request.getLines() ) {
				if(!productRepository.existsByNationalCode(lr.getNationalCode())) {
					throw new ProductNotFoundException("Product not found with nationalCode: " + lr.getNationalCode());
				}
				ReceiptLine rl = new ReceiptLine();
				rl.setReceipt(receipt);
				rl.setNationalCode(lr.getNationalCode());
				rl.setQtyExpected(lr.getQtyExpected());
				receipt.getLines().add(rl);
			}
		}

		Receipt receiptSaved = receiptRepository.save(receipt);
		return receiptMapper.toDto(receiptSaved);
	}

	@Override
	@Transactional
	public ReceiptDto addLine(Long receiptId, CreateReceiptLineRequest line) {

		Receipt receipt = checkExistingReceipt(receiptId);
		mustBeState(receipt, ReceiptState.DRAFT);
		ReceiptLine rl = new ReceiptLine();
		rl.setReceipt(receipt);
		if(!productRepository.existsByNationalCode(line.getNationalCode())) {
			throw new ProductNotFoundException("Product not found with nationalCode: " + line.getNationalCode());
		}
		else rl.setNationalCode(line.getNationalCode());
		rl.setQtyExpected(line.getQtyExpected());
		receipt.getLines().add(rl);

		receiptRepository.saveAndFlush(receipt);

		return receiptMapper.toDto(receipt);
	}

	@Override
	@Transactional
	public ReceiptDto updateLine(Long receiptId, Long lineId, UpdateReceiptLineRequest line) {

		Receipt receipt = checkExistingReceipt(receiptId);
		mustBeState(receipt, ReceiptState.DRAFT);
		ReceiptLine rl = findLine(receipt, lineId);

		if(line.getQtyExpected() != null) {
			if(line.getQtyExpected() < 0 ) throw new QuantityMustBePositiveException("qtyExpected must be >= 0");
			rl.setQtyExpected(line.getQtyExpected());
		}

		return receiptMapper.toDto(receipt);
	}

	@Override
	public ReceiptDto deleteLineToReceipt(Long receiptId, Long lineId) {

		Receipt receipt = checkExistingReceipt(receiptId);
		mustBeState(receipt, ReceiptState.DRAFT);
		ReceiptLine rl = findLine(receipt, lineId);
		receipt.getLines().remove(rl);
		receiptLineRepository.delete(rl);
		return receiptMapper.toDto(receipt);
	}

	/*
	 * VERIFYLINE: aggiorna una riga del ricevimento con quantità ricevuta, stato e motivo, rigenera i lotti (creandoli o sostituendoli)
	 * e controlla che la somma delle quantità dei lotti coincida con la quantità ricevuta; se tutte le righe risultano verificate,
	 * porta la testata Receipt a VERIFIED. La definizione dello slotCode è opzionale.
	 */
	@Override
	@Transactional
	public ReceiptDto verifyLine(Long receiptId, Long lineId, VerifyReceiptLineRequest request) {

		Receipt receipt = checkExistingReceipt(receiptId);

		if (receipt.getState() != ReceiptState.DRAFT && receipt.getState() != ReceiptState.VERIFIED)
			throw new InvalidStateTransitionException("Verify allowed only in DRAFT or partial VERIFIED");

		ReceiptLine rl = findLine(receipt, lineId);

		rl.setQtyReceived(request.getQtyReceived());
		rl.setStatus(request.getStatus());
		rl.setReason(request.getReason());

		if (request.getPutwaySlotCode() != null) {
		    Slot slot = resolveAndValidatePutawaySlot(request.getPutwaySlotCode(), rl.getNationalCode());
		    rl.setPutawaySlot(slot);
		}

		rl.getLots().clear();
		if(request.getQtyReceived() > 0) {
			if(request.getLots() == null || request.getLots().isEmpty()) {
				Lot lot = new Lot();
				lot.setReceiptLine(rl);
				lot.setQuantity(request.getQtyReceived());
				lot.setNotes(request.getReason());
				lot.setLotCode(generateLotCode(rl));
				rl.getLots().add(lot);
			}else {
				int sum = request.getLots().stream()
						.mapToInt(lr -> lr.getQuantity() != null ? lr.getQuantity() : 0)
						.sum();
				if(sum != request.getQtyReceived()) {
					throw new NonCompliantQuantityException("Sum of lot quantities must equal qtyReceived");
				}

				for(CreateLotRequest lr : request.getLots()) {
					if(lr.getLotCode() != null && lotRepository.existsByLotCode(lr.getLotCode())) {
						throw new LotAlreadyExistsException("Lot code already exists: " + lr.getLotCode());
					}
				}

				for (CreateLotRequest lr : request.getLots()) {
					Lot lot = new Lot();
					lot.setReceiptLine(rl);
					lot.setQuantity(lr.getQuantity());
					lot.setExpiryDate(lr.getExpiryDate());
					lot.setNotes(lr.getNotes());
					lot.setLotCode(lr.getLotCode() != null ? lr.getLotCode() : generateLotCode(rl));
					rl.getLots().add(lot);
				}
			}
		}

		if (allLinesVerified(receipt)) {
            receipt.setState(ReceiptState.VERIFIED);
            if (receipt.getReceivedAt() == null) {
                receipt.setReceivedAt(LocalDateTime.now());
            }
        }

		receiptLineRepository.saveAndFlush(rl);
        return receiptMapper.toDto(receipt);
	}

	@Override
	@Transactional
	public ReceiptDto completeVerification(Long receiptId) {

		Receipt receipt = checkExistingReceipt(receiptId);

		if (receipt.getState() != ReceiptState.DRAFT && receipt.getState() != ReceiptState.VERIFIED)
			throw new InvalidStateTransitionException("Verify allowed only in DRAFT or partial VERIFIED");

		if(!allLinesVerified(receipt))
			throw new LinesVerifiedException("Not all lines are verified");

		receipt.setState(ReceiptState.VERIFIED);
        if (receipt.getReceivedAt() == null) {
            receipt.setReceivedAt(LocalDateTime.now());
        }

        return receiptMapper.toDto(receipt);
	}

	/*
	 * POSTRECEIPT: prende un Receipt in stato VERIFIED, crea i movimenti di magazzino (InventoryMovement) in entrata per ogni riga con quantità ricevuta positiva escludendo quelle DAMAGED,
	 * traccia origine e timestamp, imposta lo stato della testata a POSTED restituendo un riepilogo dell’operazione. Se viene defito lo slotCode come parametro si effettuano i controlli sulla category,
	 * nel caso in cui non venga passato nulla, viene assegnato uno slotCode in automatico in a base alla disponibilità.
	 */
	@Override
	@Transactional
	public PostReceiptResponse postReceipt(Long receiptId, List<SlotDefiniedAtPost> listSlotAtPost) {

		Receipt receipt = checkExistingReceipt(receiptId);

		if (receipt.getState() != ReceiptState.VERIFIED) {
			throw new InvalidStateTransitionException("Receipt must be VERIFIED to post");
		}

		if(!listSlotAtPost.isEmpty()) {
			for(ReceiptLine rl : receipt.getLines()) {
				if(rl.getPutawaySlot() == null) {
					int index = findIntoList(rl.getNationalCode(), listSlotAtPost);
					if( index != -1) {

						SlotDefiniedAtPost s = listSlotAtPost.get(index);
						Slot slot = resolveAndValidatePutawaySlot(s.getSlotCode(), rl.getNationalCode());
						rl.setPutawaySlot(slot);

					}else resolveValidatePutawaySlot(rl);
				}
			}
		}
		else {
			for(ReceiptLine rl : receipt.getLines())
				if(rl.getPutawaySlot() == null) resolveValidatePutawaySlot(rl);
		}

		int created = 0;

		for(ReceiptLine rl : receipt.getLines())
			if(rl.getPutawaySlot()==null)
				throw new InvalidStateTransitionException("Receipt: " + receiptId + " cannot be posted. ReceiptLine: " + rl.getId() + " has putawaySlot equals NULL");

		for (ReceiptLine rl : receipt.getLines()) {
			int qty = rl.getQtyReceived() != null ? rl.getQtyReceived() : 0;
			if (qty <= 0) continue;
			if (rl.getStatus() == ReceiptLineStatus.DAMAGED) continue;

			storeMovement(rl, MovementType.INBOUND_RECEIPT, qty, receipt.getId());

			created++;
		}

		receipt.setState(ReceiptState.POSTED);
		LocalDateTime now = LocalDateTime.now();

		PostReceiptResponse resp = new PostReceiptResponse();
		resp.setReceiptId(receipt.getId());
		resp.setMovementCount(created);
		resp.setPostedAt(now);
		return resp;
	}

	@Override
	@Transactional
	public ReceiptDto cancel(Long receiptId) {

		Receipt receipt = checkExistingReceipt(receiptId);

		if (receipt.getState() == ReceiptState.POSTED)
			throw new InvalidStateTransitionException("Cannot cancel a POSTED receipt");
		receipt.setState(ReceiptState.CANCELED);
		return receiptMapper.toDto(receipt);
	}

	@Override
    public ReceiptDto getReceiptById(Long receiptId) {
        return receiptMapper.toDto(
        		receiptRepository.findById(receiptId)
				.orElseThrow(() -> new ReceiptNotFoundException("Receipt " + receiptId + " not found"))
        );
    }

	@Override
	public List<ReceiptDto> listReceipt() {
		return receiptRepository.findAll().stream()
				.map(receiptMapper::toDto)
				.toList();
	}

	@Override
	public ReceiptDto deleteReceipt(Long receiptId) {

		Receipt receipt = receiptRepository.findById(receiptId)
				.orElseThrow(() -> new ReceiptNotFoundException("Receipt " + receiptId + " not found"));

		receiptRepository.delete(receipt);
		return receiptMapper.toDto(receipt);
	}

	//-----------> HELPERS <-----------
	private void mustBeState(Receipt r, ReceiptState expected) {
        if (r.getState() != expected)
            throw new InvalidStateTransitionException("Receipt must be " + expected + " but is " + r.getState());
    }

	private ReceiptLine findLine(Receipt r, Long lineId) {
        return r.getLines().stream()
                .filter(l -> Objects.equals(l.getId(), lineId))
                .findFirst()
                .orElseThrow(() -> new ReceiptLineNotFoundToReceiptException("Line " + lineId + " not found on receipt " + r.getId()));
	}

	private boolean allLinesVerified(Receipt r) {
		if (r.getLines() == null || r.getLines().isEmpty()) return false;
		for (ReceiptLine rl : r.getLines()) {
			if (rl.getQtyReceived() == null || rl.getStatus() == null) return false;
			if ((rl.getQtyReceived() > 0) && (rl.getLots() == null || rl.getLots().isEmpty())) return false;
			int sumLots = rl.getLots().stream().mapToInt(l -> l.getQuantity() != null ? l.getQuantity() : 0).sum();
			if (rl.getQtyReceived() > 0 && sumLots != rl.getQtyReceived()) return false;
		}
		return true;
	}

	private String generateLotCode(ReceiptLine rl) {
		String base = "L" + java.time.LocalDate.now().toString().replace("-", "");
		String rcpt = String.valueOf(rl.getReceipt() != null ? rl.getReceipt().getId() : 0);
		String line = String.valueOf(rl.getId());
		String rand = java.util.UUID.randomUUID().toString().substring(0, 6).toUpperCase();
		return base + "-" + rcpt + "-" + line + "-" + rand;
	}

	private int findIntoList(String nationalCode, List<SlotDefiniedAtPost> listSlotAtPost) {
		int index=0;
		for(SlotDefiniedAtPost s : listSlotAtPost) {
			if(s.getNationalCode().equals(nationalCode)) return index;
			index++;
		}
		return -1;
	}

	void storeMovement(ReceiptLine rl, MovementType type, Integer quantity, Long referenceId) {
		InventoryMovement m = new InventoryMovement();

		LocalDateTime now = LocalDateTime.now();
		m.setNationalCode(rl.getNationalCode());
		m.setQuantity(quantity);
		m.setType(type);
		m.setReferenceType("RECEIPT");
		m.setReferenceId(referenceId);
		m.setSlot(rl.getPutawaySlot());
		m.setTimestamp(now);
		movementRepository.save(m);
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

	private void resolveValidatePutawaySlot(ReceiptLine rl) {

		Product product = productRepository.findByNationalCode(rl.getNationalCode());

		Section section = sectionRepository.findByCategory(product.getCategory());

		Set<Slot> slots = section.getSlots();

		if (slots != null && !slots.isEmpty()) {
			Slot first = slots.iterator().next();
			rl.setPutawaySlot(first);
		}
		else throw new SlotNotFoundException("Slots not found fot category: " + section.getCategory());
	}

	private Receipt checkExistingReceipt(Long receiptId) {
		return receiptRepository.findById(receiptId)
				.orElseThrow(() -> new ReceiptNotFoundException("Receipt " + receiptId + " not found"));
	}
}