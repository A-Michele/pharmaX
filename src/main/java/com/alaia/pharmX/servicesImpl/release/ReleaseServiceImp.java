package com.alaia.pharmX.servicesImpl.release;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.alaia.pharmX.dtos.order.OrderDto;
import com.alaia.pharmX.dtos.order.OrderLineDto;
import com.alaia.pharmX.dtos.picking.PickListDto;
import com.alaia.pharmX.exceptions.servicesImpl.OrderNotFoundException;
import com.alaia.pharmX.mappers.order.OrderMapper;
import com.alaia.pharmX.mappers.releasing.PickListMapper;
import com.alaia.pharmX.models.Product;
import com.alaia.pharmX.models.Slot;
import com.alaia.pharmX.models.order.Order;
import com.alaia.pharmX.models.order.State;
import com.alaia.pharmX.models.picking.PickItem;
import com.alaia.pharmX.models.picking.PickList;
import com.alaia.pharmX.models.picking.PickListItemStatus;
import com.alaia.pharmX.models.picking.PickListStatus;
import com.alaia.pharmX.models.stock.InfoSlot;
import com.alaia.pharmX.models.stock.Stock;
import com.alaia.pharmX.repositories.ProductRepository;
import com.alaia.pharmX.repositories.SlotRepository;
import com.alaia.pharmX.repositories.order.OrderRepository;
import com.alaia.pharmX.repositories.picking.PickListRepository;
import com.alaia.pharmX.repositories.stock.StockRepository;
import com.alaia.pharmX.services.release.ReleaseService;

@Service
public class ReleaseServiceImp implements ReleaseService{

	@Autowired
	private PickListRepository pickListRepository;

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private SlotRepository slotRepository;

	@Autowired
	private StockRepository stockRepository;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private PickListMapper pickListMapper;

	@Autowired
	private OrderMapper orderMapper;


	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public List<PickListDto> releaseOrders(List<String> orders) {
        validateOrders(orders);

        Map<String, List<OrderDto>> cfToOrdersMap = processOrders(orders);

        List<PickListDto> pickListDtos = createAndSavePickLists(cfToOrdersMap);

        return pickListDtos;
    }

	//----> HELPERS FOR RELEASE ORDERS  <-----
    private void validateOrders(List<String> orders) {
        checkExistingReleaseOrder(orders);
    }

    private Map<String, List<OrderDto>> processOrders(List<String> orders) {
        Map<String, List<OrderDto>> map = new HashMap<>();
        for (String code : orders) {
            Order order = orderRepository.findByCode(code);
            order.setState(State.RELEASED);
            OrderDto orderDto = orderMapper.toDto(order);
            String cf = order.getCf();
            map.computeIfAbsent(cf, k -> new ArrayList<>()).add(orderDto);

            orderRepository.save(order);
        }
        return map;
    }

    private List<PickListDto> createAndSavePickLists(Map<String, List<OrderDto>> cfToOrdersMap) {
        List<PickListDto> pickListDtos = new ArrayList<>();
        for (Map.Entry<String, List<OrderDto>> entry : cfToOrdersMap.entrySet()) {
            String cf = entry.getKey();
            List<OrderDto> orderList = entry.getValue();

            PickList pickList = createPickListForCF(cf, orderList);
            pickList = pickListRepository.save(pickList);
            pickListDtos.add(pickListMapper.toDto(pickList));
        }
        return pickListDtos;
    }

    private PickList createPickListForCF(String cf, List<OrderDto> orderList) {
        PickList pickList = new PickList();
        pickList.setCode(generatePickListCode(cf));
        pickList.setState(PickListStatus.OPEN);
        pickList.setCreatedAt(LocalDateTime.now());
        pickList.setLastModification(LocalDateTime.now());
        pickList.setCf(cf);

        List<PickItem> listPickItem = createPickItems(orderList, pickList);
        pickList.setItems(listPickItem);
        return pickList;
    }

    private List<PickItem> createPickItems(List<OrderDto> orderList, PickList pickList) {
        List<PickItem> listPickItem = new ArrayList<>();
        for (OrderDto dto : orderList) {
            for (OrderLineDto line : dto.getOrderLines()) {
                PickItem p = new PickItem();
                p.setState(PickListItemStatus.OPEN);
                p.setQuantityToPick(line.getQuantity());
                p.setNationalCode(line.getNationalCode());

                Stock stock = stockRepository.findByNationalCode(line.getNationalCode());
                List<InfoSlot> infoSlots = stock.getInfoSlots();

                StringJoiner slotsCodeJoiner = new StringJoiner(" | ");
                for (InfoSlot info : infoSlots) {
                    slotsCodeJoiner.add(info.getSlotCode());
                }
                p.setSlotsCode(slotsCodeJoiner.toString());

                Slot slot = slotRepository.findByCode(infoSlots.get(0).getSlotCode()).get();
                p.setPickingSequence(slot.getPickingSequence());
                p.setCode(generatePickItemCode(line.getNationalCode()));

                Product product = productRepository.findByNationalCode(line.getNationalCode());
                p.setNameProduct(product.getName());
                p.setReason(null);
                p.setCodeOrder(dto.getCode());
                p.setLineNumber(line.getLineNumber());
                p.setPickList(pickList);

                listPickItem.add(p);
            }
        }
        return listPickItem;
    }

    private String generatePickListCode(String cf) {
        String base = "PL" + LocalDate.now().toString().replace("-", "");
        String rand = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return base + "-" + cf.substring(0, 3) + "-" + rand;
    }

    private String generatePickItemCode(String nationalCode) {
        String rand = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return nationalCode.substring(4, 9) + "-" + rand;
    }

    private void checkExistingReleaseOrder(List<String> orders) {

    	for (String s : orders) {
            Order order = orderRepository.findByCode(s);
            if (order != null && !order.getState().equals(State.OPEN)) {
                throw new IllegalArgumentException("Order must be in OPEN state");
            }
            if (order == null) {
                throw new OrderNotFoundException("Order: " + s + " not found");
            }
        }
    }
}
