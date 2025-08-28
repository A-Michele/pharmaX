package com.alaia.pharmX.servicesImpl.receiving;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.alaia.pharmX.dtos.receiving.MovementDto;
import com.alaia.pharmX.dtos.receiving.StockBySlotDto;
import com.alaia.pharmX.dtos.receiving.StockItemDto;
import com.alaia.pharmX.exceptions.servicesImpl.OrderNotFoundException;
import com.alaia.pharmX.exceptions.servicesImpl.ProductNotFoundException;
import com.alaia.pharmX.exceptions.servicesImpl.ReceiptNotFoundException;
import com.alaia.pharmX.exceptions.servicesImpl.StockNotAvailableException;
import com.alaia.pharmX.mappers.receiving.InventoryMovementMapper;
import com.alaia.pharmX.models.receiving.InventoryMovement;
import com.alaia.pharmX.repositories.ProductRepository;
import com.alaia.pharmX.repositories.order.OrderRepository;
import com.alaia.pharmX.repositories.receiving.InventoryMovementRepository;
import com.alaia.pharmX.repositories.receiving.ReceiptRepository;
import com.alaia.pharmX.services.receiving.InventoryService;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryServiceImpl implements InventoryService {

	@Autowired
	private ReceiptRepository receiptRepository;

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private InventoryMovementRepository movementRepository;

	@Autowired
	private InventoryMovementMapper movementMapper;

	@Autowired
	private ProductRepository productRepository;

	@Override
	@Transactional
	public List<MovementDto> getMovementsByReceipt(Long receiptId) {

		if(!receiptRepository.existsById(receiptId))
			throw new ReceiptNotFoundException("Receipt with ID: " + receiptId + " not found");
		List<InventoryMovement> inventoryMovement = movementRepository.findByReferenceTypeAndReferenceId("RECEIPT", receiptId);
		return inventoryMovement.stream()
				.map(movementMapper::toDto)
				.toList();
	}

	@Override
	@Transactional
	public List<MovementDto> getMovementsByOrder(Long orderId) {

		if(!orderRepository.existsById(orderId))
			throw new OrderNotFoundException("Order with ID: " + orderId + " not found");
		List<InventoryMovement> inventoryMovement = movementRepository.findByReferenceTypeAndReferenceId("ORDER", orderId);
		return inventoryMovement.stream()
				.map(movementMapper::toDto)
				.toList();
	}

	@Override
	public List<StockItemDto> getStock() {
		return movementRepository.findStockDto();
	}

	@Override
	public List<StockItemDto> getStockAsOf(LocalDateTime asOf) {
		 return movementRepository.findStockDtoAsOf(asOf);
	}

	@Override
	public List<MovementDto> getMovementsByNationalCode(String nationalCode){
		if(!productRepository.existsByNationalCode(nationalCode))
			throw new ProductNotFoundException("Product not found with national code : " + nationalCode);

		List<InventoryMovement> list = movementRepository.findByNationalCode(nationalCode);
		return list.stream()
				.map(movementMapper::toDto)
				.toList();
	}

	@Override
	public StockItemDto getStokOfNationalCode(String nationalCode) {
		if(!productRepository.existsByNationalCode(nationalCode))
			throw new ProductNotFoundException("Product not found with national code : " + nationalCode);

		List<InventoryMovement> list = movementRepository.findByNationalCode(nationalCode);
		if(list.isEmpty()) throw new StockNotAvailableException("Stock not available for product: " + nationalCode);

		return movementRepository.findStockDtoByNationalCode(nationalCode);
	}

	@Override
    @Transactional
    public List<StockBySlotDto> getStockBySlot() {
        return movementRepository.sumByProductAndSlot();
    }
}