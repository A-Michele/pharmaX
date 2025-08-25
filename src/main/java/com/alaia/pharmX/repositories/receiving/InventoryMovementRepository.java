package com.alaia.pharmX.repositories.receiving;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.alaia.pharmX.dtos.receiving.StockBySlotDto;
import com.alaia.pharmX.dtos.receiving.StockItemDto;
import com.alaia.pharmX.models.receiving.InventoryMovement;

public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Long> {

	List<InventoryMovement> findByReferenceTypeAndReferenceId(String string, Long receiptId);

	@Query("""
			select new com.alaia.pharmX.dtos.receiving.StockItemDto(
			    m.nationalCode,
			    cast(sum(m.quantity) as integer)
			)
			from InventoryMovement m
			group by m.nationalCode
			""")
	List<StockItemDto> findStockDto();

	@Query("""
			select new com.alaia.pharmX.dtos.receiving.StockItemDto(
			    m.nationalCode,
			    cast(sum(m.quantity) as integer)
			)
			from InventoryMovement m
			where m.timestamp <= :asOf
			group by m.nationalCode
			""")
	List<StockItemDto> findStockDtoAsOf(LocalDateTime asOf);

	List<InventoryMovement> findByNationalCode(String nationalCode);

	@Query("""
			select new com.alaia.pharmX.dtos.receiving.StockItemDto(
			    m.nationalCode,
			    cast(sum(m.quantity) as integer)
			)
			from InventoryMovement m
			where m.nationalCode = :nationalCode
			group by m.nationalCode
			""")
	StockItemDto findStockDtoByNationalCode(@Param("nationalCode") String nationalCode);

	@Query("""
			    select new com.alaia.pharmX.dtos.receiving.StockBySlotDto(
			        m.nationalCode,
			        m.slot.code,
			        cast(coalesce(sum(m.quantity), 0L) as integer)
			    )
			    from InventoryMovement m
			    group by m.nationalCode, m.slot.code
			""")
	List<StockBySlotDto> sumByProductAndSlot();

}