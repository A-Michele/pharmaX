package com.alaia.pharmX.servicesImpl.stock;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;

import com.alaia.pharmX.dtos.stock.AvailableQuantityProduct;
import com.alaia.pharmX.dtos.stock.EffectiveQuantityProduct;
import com.alaia.pharmX.dtos.stock.ReservedQuantityProduct;
import com.alaia.pharmX.dtos.stock.StockDto;
import com.alaia.pharmX.dtos.stock.StockOperation;
import com.alaia.pharmX.exceptions.servicesImpl.ProductAlreadyExistsException;
import com.alaia.pharmX.exceptions.servicesImpl.ProductNotFoundException;
import com.alaia.pharmX.exceptions.servicesImpl.ProductOutOfStockException;
import com.alaia.pharmX.exceptions.servicesImpl.StockNotAvailableException;
import com.alaia.pharmX.mappers.stock.StockMapper;
import com.alaia.pharmX.models.receiving.InventoryMovement;
import com.alaia.pharmX.models.receiving.MovementType;
import com.alaia.pharmX.models.receiving.QuantityType;
import com.alaia.pharmX.models.stock.Stock;
import com.alaia.pharmX.repositories.ProductRepository;
import com.alaia.pharmX.repositories.receiving.InventoryMovementRepository;
import com.alaia.pharmX.repositories.stock.StockRepository;
import com.alaia.pharmX.services.stock.StockService;

import org.springframework.transaction.annotation.Transactional;

@Service
public class StockServiceImpl implements StockService{

	@Autowired
	private InventoryMovementRepository movementRepository;

	@Autowired
	private StockRepository stockRepository;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private StockMapper stockMapper;

	@Override
	public EffectiveQuantityProduct getEffectiveQuantity(String nationalCode) {
		if(!stockRepository.existsByNationalCode(nationalCode)) {
			throw new ProductOutOfStockException("Product: " + nationalCode + ", is out of stock");
		}

		Stock stock = stockRepository.findByNationalCode(nationalCode);
		return new EffectiveQuantityProduct(nationalCode, stock.getEffectiveQuantity());
	}

	@Override
	public ReservedQuantityProduct getReservedQuantity(String nationalCode) {
		if(!stockRepository.existsByNationalCode(nationalCode)) {
			throw new ProductOutOfStockException("Product: " + nationalCode + ", is out of stock");
		}

		Stock stock = stockRepository.findByNationalCode(nationalCode);
		return new ReservedQuantityProduct(nationalCode, stock.getReservedQuantity());
	}

	@Override
	public AvailableQuantityProduct getAvailableQuantity(String nationalCode) {
		if(!stockRepository.existsByNationalCode(nationalCode)) {
			throw new ProductOutOfStockException("Product: " + nationalCode + ", is out of stock");
		}

		Stock stock = stockRepository.findByNationalCode(nationalCode);
		return new AvailableQuantityProduct(nationalCode, stock.getEffectiveQuantity() - stock.getReservedQuantity());
	}

	//PER CREAZIONE ORDINI
	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public StockDto reserveQuantity(StockOperation operation) {
		Stock stock = stockRepository.findByNationalCode(operation.getNationalCode());
		if (stock == null)
			throw new ProductOutOfStockException("Product: " + operation.getNationalCode() + ", is out of stock");

		int availableQuantity = stock.getEffectiveQuantity() - stock.getReservedQuantity();
		if( operation.getQuantity() > availableQuantity)
			throw new StockNotAvailableException("[" + operation.getNationalCode() + "] Requested quantity : " + operation.getQuantity() + " > Available quantity: " + availableQuantity);
		else {
			stock.setReservedQuantity( stock.getReservedQuantity() + operation.getQuantity());
			storeMovement(operation);
		}
		Stock stockSaved = stockRepository.save(stock);
		return stockMapper.toDto(stockSaved);
	}

	//PER RECEIPT
	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public StockDto onReceiptOpration(StockOperation operation) {
		Stock stock = stockRepository.findByNationalCode(operation.getNationalCode());
		if (stock == null) {
			LocalDateTime now = LocalDateTime.now();

			stock = new Stock();
			stock.setNationalCode(operation.getNationalCode());
			stock.setEffectiveQuantity(operation.getQuantity());
			stock.setReservedQuantity(0);
			stock.setLastModification(now);

		}
		else {
			stock.setEffectiveQuantity(stock.getEffectiveQuantity() + operation.getQuantity());
		}

		storeMovement(operation);

		Stock stockSaved = stockRepository.save(stock);
		return stockMapper.toDto(stockSaved);
	}

	//PER PICKING
	@Override
	public StockDto unReserveQuantity(StockOperation operation) {
		Stock stock = stockRepository.findByNationalCode(operation.getNationalCode());
		if (stock == null)
			throw new ProductOutOfStockException("Product: " + operation.getNationalCode() + ", is out of stock");

		if(operation.getQuantity() > 0) {
			stock.setReservedQuantity( stock.getReservedQuantity() - operation.getQuantity());
			stock.setEffectiveQuantity( stock.getEffectiveQuantity() - operation.getQuantity());
			storeMovement(operation);
		}

		Stock stockSaved = stockRepository.save(stock);
		return stockMapper.toDto(stockSaved);
	}

	//PER CANCELLAZIONE O DELETE
	@Override
	public StockDto unReserveQuantityOnDeleteOrCanceled(StockOperation operation) {
		Stock stock = stockRepository.findByNationalCode(operation.getNationalCode());
		if (stock == null)
			throw new ProductOutOfStockException("Product: " + operation.getNationalCode() + ", is out of stock");

		if(operation.getQuantity() > 0) {
			stock.setReservedQuantity( stock.getReservedQuantity() - operation.getQuantity());
			storeMovement(operation);
		}

		Stock stockSaved = stockRepository.save(stock);
		return stockMapper.toDto(stockSaved);
	}

	@Override
	public StockDto createStock(StockDto stockDto) {
		if(stockRepository.existsByNationalCode(stockDto.getNationalCode())) {
			throw new ProductAlreadyExistsException("Product already exists in stock with nationalCode: " + stockDto.getNationalCode());
		}

		if(!productRepository.existsByNationalCode(stockDto.getNationalCode())) {
			throw new ProductNotFoundException("Product not found with nationalCode: " + stockDto.getNationalCode());
		}

		if(stockDto.getReservedQuantity() > stockDto.getEffectiveQuantity())
			throw new IllegalArgumentException("The reserved quantity: " + stockDto.getReservedQuantity() + " cannot be greater than the effective quantity: " + stockDto.getEffectiveQuantity());

		LocalDateTime now = LocalDateTime.now();
		stockDto.setLastModification(now);

		Stock entity = stockMapper.toEntity(stockDto);

		Stock stockSaved = stockRepository.save(entity);

		InventoryMovement m = new InventoryMovement();
		m.setNationalCode(stockDto.getNationalCode());
		m.setQuantity(stockDto.getEffectiveQuantity() - stockDto.getReservedQuantity());
		m.setType(MovementType.ADJUSTMENT );
		m.setReferenceType("STOCK");
		m.setReferenceId(stockSaved.getId());
		m.setTimestamp(now);
		movementRepository.save(m);


		return stockMapper.toDto(stockSaved);
	}

	@Override
	public List<StockDto> getAllStock() {
		return stockRepository.findAll().stream()
				.map(stockMapper::toDto)
				.toList();
	}


	//--------HELPER-----

	void storeMovement(StockOperation operation) {

		LocalDateTime now = LocalDateTime.now();
		switch(operation.getType()) {
		case MovementType.ORDER_ALLOCATION:
			InventoryMovement allocationMovement = new InventoryMovement();

			allocationMovement.setNationalCode(operation.getNationalCode());
			allocationMovement.setType(operation.getType());

			allocationMovement.setTypeQuantity(QuantityType.RESERVED);
			allocationMovement.setQuantity(operation.getQuantity());

			allocationMovement.setReferenceType(operation.getReferenceType());
			allocationMovement.setReferenceId(operation.getReferenceId());
			allocationMovement.setTimestamp(now);

			movementRepository.save(allocationMovement);
			break;
		case MovementType.INBOUND_RECEIPT:
			InventoryMovement receiptMovement = new InventoryMovement();

			receiptMovement.setNationalCode(operation.getNationalCode());
			receiptMovement.setType(operation.getType());

			receiptMovement.setTypeQuantity(QuantityType.EFFECTIVE);
			receiptMovement.setQuantity(operation.getQuantity());

			receiptMovement.setReferenceType(operation.getReferenceType());
			receiptMovement.setReferenceId(operation.getReferenceId());
			receiptMovement.setTimestamp(now);

			movementRepository.save(receiptMovement);
			break;
		case MovementType.PICKING:
			InventoryMovement pickingMovementOne = new InventoryMovement();
			InventoryMovement pickingMovementTwo = new InventoryMovement();

			pickingMovementOne.setNationalCode(operation.getNationalCode());
			pickingMovementTwo.setNationalCode(operation.getNationalCode());

			pickingMovementOne.setType(operation.getType());
			pickingMovementTwo.setType(operation.getType());

			pickingMovementOne.setTypeQuantity(QuantityType.EFFECTIVE);
			pickingMovementOne.setQuantity(-operation.getQuantity());
			pickingMovementTwo.setTypeQuantity(QuantityType.RESERVED);
			pickingMovementTwo.setQuantity(-operation.getQuantity());

			pickingMovementOne.setReferenceType(operation.getReferenceType());
			pickingMovementTwo.setReferenceType(operation.getReferenceType());
			pickingMovementOne.setReferenceId(operation.getReferenceId());
			pickingMovementTwo.setReferenceId(operation.getReferenceId());
			pickingMovementOne.setTimestamp(now);
			pickingMovementTwo.setTimestamp(now);

			movementRepository.save(pickingMovementOne);
			movementRepository.save(pickingMovementTwo);

			break;
		case MovementType.ADJUSTMENT:
			InventoryMovement adjustmentMovement = new InventoryMovement();

			adjustmentMovement.setNationalCode(operation.getNationalCode());
			adjustmentMovement.setType(operation.getType());

			adjustmentMovement.setTypeQuantity(QuantityType.RESERVED);
			adjustmentMovement.setQuantity(operation.getQuantity());

			adjustmentMovement.setReferenceType(operation.getReferenceType());
			adjustmentMovement.setReferenceId(operation.getReferenceId());
			adjustmentMovement.setTimestamp(now);

			movementRepository.save(adjustmentMovement);
			break;

		case MovementType.RETURN:
			InventoryMovement returnMovement = new InventoryMovement();

			returnMovement.setNationalCode(operation.getNationalCode());
			returnMovement.setType(operation.getType());

			returnMovement.setTypeQuantity(QuantityType.RESERVED);
			returnMovement.setQuantity(operation.getQuantity());

			returnMovement.setReferenceType(operation.getReferenceType());
			returnMovement.setReferenceId(operation.getReferenceId());
			returnMovement.setTimestamp(now);

			movementRepository.save(returnMovement);
			break;
		}
	}
}