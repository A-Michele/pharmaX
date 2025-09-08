package com.alaia.pharmX.order.controller;

import com.alaia.pharmX.dtos.order.FilterOrdersToRelease;
import com.alaia.pharmX.dtos.order.OrderDto;
import com.alaia.pharmX.dtos.order.OrderLineDto;
import com.alaia.pharmX.dtos.stock.AvailableQuantityProduct;
import com.alaia.pharmX.dtos.stock.StockDto;
import com.alaia.pharmX.dtos.stock.StockOperation;
import com.alaia.pharmX.models.order.State;
import com.alaia.pharmX.services.stock.StockService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
public class OrderControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    private StockService stockService;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    // Test per POST /order

    @Test
    void testCreateOrder_Success() throws Exception {
        OrderLineDto lineDto = new OrderLineDto(0L, "ABD123", 3, null, null);
        OrderDto orderDto = new OrderDto(0L, null, State.OPEN, "CF123", LocalDateTime.now(), Set.of(lineDto));

        AvailableQuantityProduct availableQuantity = new AvailableQuantityProduct("ABD123", 10);
        when(stockService.getAvailableQuantity("ABD123")).thenReturn(availableQuantity);
        StockDto stockDto = new StockDto();
        stockDto.setNationalCode("ABD123");
        stockDto.setEffectiveQuantity(10);
        stockDto.setReservedQuantity(3);
        when(stockService.reserveQuantity(any(StockOperation.class))).thenReturn(stockDto);

        MvcResult result = mockMvc.perform(post("/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cf").value("CF123"))
                .andExpect(jsonPath("$.state").value("OPEN"))
                .andExpect(jsonPath("$.orderLines[0].nationalCode").value("ABD123"))
                .andExpect(jsonPath("$.orderLines[0].quantity").value(3))
                .andReturn();

        OrderDto responseDto = objectMapper.readValue(result.getResponse().getContentAsString(), OrderDto.class);
        assertNotNull(responseDto);
        assertEquals("CF123", responseDto.getCf());
        assertEquals(State.OPEN, responseDto.getState());
        assertEquals(1, responseDto.getOrderLines().size());
        assertEquals("ABD123", responseDto.getOrderLines().iterator().next().getNationalCode());
        assertEquals(3, responseDto.getOrderLines().iterator().next().getQuantity());
    }

    @Test
    void testCreateOrder_StockNotAvailable() throws Exception {
        OrderLineDto lineDto = new OrderLineDto(0L, "ABD123", 3, null, null);
        OrderDto orderDto = new OrderDto(0L, null, State.OPEN, "CF123", LocalDateTime.now(), Set.of(lineDto));
        when(stockService.getAvailableQuantity("ABD123")).thenReturn(null);

        mockMvc.perform(post("/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderDto)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void testCreateOrder_InvalidCustomer() throws Exception {
        OrderLineDto lineDto = new OrderLineDto(0L, "ABD123", 3, null, null);
        OrderDto orderDto = new OrderDto(0L, null, State.OPEN, "INVALID_CF", LocalDateTime.now(), Set.of(lineDto));
    	when(stockService.getAvailableQuantity("ABD123")).thenReturn(new AvailableQuantityProduct("ABD123", 10));

        mockMvc.perform(post("/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateOrder_ProductNotFound() throws Exception {
        OrderLineDto lineDto = new OrderLineDto(0L, "INVALID_NC", 3, null, null);
        OrderDto orderDto = new OrderDto(0L, null, State.OPEN, "CF123", LocalDateTime.now(), Set.of(lineDto));

        mockMvc.perform(post("/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderDto)))
                .andExpect(status().isNotFound());
    }

    // Test per GET /order/order_id/{id}

    @Test
    void testGetOrderById_Success() throws Exception {
    	MvcResult result = mockMvc.perform(get("/order/order_id/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("ORD-CF-123"))
                .andExpect(jsonPath("$.state").value("OPEN"))
                .andExpect(jsonPath("$.cf").value("CF123"))
                .andExpect(jsonPath("$.orderLines[0].nationalCode").value("ABC123"))
                .andExpect(jsonPath("$.orderLines[0].lineNumber").value("ORDLINE-123456"))
                .andReturn();

    	OrderDto responseDto = objectMapper.readValue(result.getResponse().getContentAsString(), OrderDto.class);
        assertNotNull(responseDto);
        assertEquals("ORD-CF-123", responseDto.getCode());
        assertEquals("CF123", responseDto.getCf());
        assertEquals(State.OPEN, responseDto.getState());
        assertEquals(1, responseDto.getOrderLines().size());
        assertEquals("ABC123", responseDto.getOrderLines().iterator().next().getNationalCode());
        assertEquals("ORDLINE-123456", responseDto.getOrderLines().iterator().next().getLineNumber());
    }

    @Test
    void testGetOrderById_NotFound() throws Exception {
        mockMvc.perform(get("/order/order_id/999"))
                .andExpect(status().isNotFound());
    }

    // Test per GET /order/order_code/{code}

    @Test
    void testGetOrderByCode_Success() throws Exception {
    	mockMvc.perform(get("/order/order_code/ORD-CF-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("ORD-CF-123"))
                .andExpect(jsonPath("$.state").value("OPEN"))
                .andExpect(jsonPath("$.orderLines[0].nationalCode").value("ABC123"));
    }

    @Test
    void testGetOrderByCode_NotFound() throws Exception {
    	mockMvc.perform(get("/order/order_code/INVALID"))
    	.andExpect(status().isNotFound());
    }

    // Test per GET /order/all

    @Test
    void testGetAllOrders_Success() throws Exception {
        mockMvc.perform(get("/order/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].code").value("ORD-CF-123"))
                .andExpect(jsonPath("$[1].code").value("ORD-CF-NOP"))
                .andExpect(jsonPath("$[5].code").value("ORD-NC-INVALID"));
    }

    // Test per POST /order/addLine/{code}

    @Test
    void testAddLine_Success() throws Exception {
        OrderLineDto lineDto = new OrderLineDto(0L, "XYZ123", 2, null, null);

        AvailableQuantityProduct availableQuantity = new AvailableQuantityProduct("XYZ123", 10);
        when(stockService.getAvailableQuantity("XYZ123")).thenReturn(availableQuantity);
        StockDto stockDto = new StockDto();
        stockDto.setNationalCode("XYZ123");
        stockDto.setEffectiveQuantity(10);
        stockDto.setReservedQuantity(2);
        when(stockService.reserveQuantity(any(StockOperation.class))).thenReturn(stockDto);

        MvcResult result = mockMvc.perform(post("/order/addLine/ORD-CF-123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(lineDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("ORD-CF-123"))
                .andExpect(jsonPath("$.orderLines[0].nationalCode").value("XYZ123"))
                .andExpect(jsonPath("$.orderLines[0].quantity").value(2))
                .andReturn();

        OrderDto responseDto = objectMapper.readValue(result.getResponse().getContentAsString(), OrderDto.class);
        assertNotNull(responseDto);
        assertEquals("ORD-CF-123", responseDto.getCode());
        assertEquals(2, responseDto.getOrderLines().size());
        assertTrue(responseDto.getOrderLines().stream().anyMatch(line -> "XYZ123".equals(line.getNationalCode()) && line.getQuantity() == 2));
    }

    @Test
    void testAddLine_OrderNotFound() throws Exception {
        OrderLineDto lineDto = new OrderLineDto(0L, "XYZ123", 2, null, null);
        mockMvc.perform(post("/order/addLine/INVALID")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(lineDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testAddLine_NonOpenOrder() throws Exception {
    	OrderLineDto lineDto = new OrderLineDto(0L, "XYZ123", 2, null, null);

    	AvailableQuantityProduct availableQuantity = new AvailableQuantityProduct("XYZ123", 10);
        when(stockService.getAvailableQuantity("XYZ123")).thenReturn(availableQuantity);
        StockDto stockDto = new StockDto();
        stockDto.setNationalCode("XYZ123");
        stockDto.setEffectiveQuantity(10);
        stockDto.setReservedQuantity(2);
        when(stockService.reserveQuantity(any(StockOperation.class))).thenReturn(stockDto);

        mockMvc.perform(post("/order/addLine/ORD-CF-PICKING")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(lineDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testAddLine_ProductNotFound() throws Exception {
    	OrderLineDto lineDto = new OrderLineDto(0L, "INVALID_NC", 2, null, null);
    	mockMvc.perform(post("/order/addLine/ORD-CF-123")
    			.contentType(MediaType.APPLICATION_JSON)
    			.content(objectMapper.writeValueAsString(lineDto)))
    			.andExpect(status().isNotFound());
    }

    // Test per PATCH /order/lines/{orderLineId}/update

    @Test
    void testUpdateLineQuantity_Success() throws Exception {
        AvailableQuantityProduct availableQuantity = new AvailableQuantityProduct("ABC123", 10);
        when(stockService.getAvailableQuantity("ABC123")).thenReturn(availableQuantity);

        StockDto stockDto = new StockDto();
        stockDto.setNationalCode("ABC123");
        stockDto.setEffectiveQuantity(10);
        stockDto.setReservedQuantity(7);
        when(stockService.reserveQuantity(any(StockOperation.class))).thenReturn(stockDto);
        when(stockService.unReserveQuantityOnDeleteOrCanceled(any(StockOperation.class))).thenReturn(stockDto);

        mockMvc.perform(patch("/order/lines/1/update")
                .param("quantity", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderLines[0].quantity").value(7));
    }

    @Test
    void testUpdateLineQuantity_StockNotAvailable() throws Exception {
        when(stockService.getAvailableQuantity("ABC123")).thenReturn(null);

        mockMvc.perform(patch("/order/lines/1/update")
                .param("quantity", "7"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void testUpdateLineQuantity_NonOpenOrder() throws Exception {
        AvailableQuantityProduct availableQuantity = new AvailableQuantityProduct("ABC123", 10);
        when(stockService.getAvailableQuantity("ABC123")).thenReturn(availableQuantity);
        StockDto stockDto = new StockDto();
        stockDto.setNationalCode("ABC123");
        stockDto.setEffectiveQuantity(10);
        stockDto.setReservedQuantity(7);
        when(stockService.reserveQuantity(any(StockOperation.class))).thenReturn(stockDto);
        when(stockService.unReserveQuantityOnDeleteOrCanceled(any(StockOperation.class))).thenReturn(stockDto);

        mockMvc.perform(patch("/order/lines/5/update")
                .param("quantity", "7"))
                .andExpect(status().isBadRequest());
    }

    // Test per DELETE /order/lines/{orderLineId}
    @Test
    void testRemoveLine_Success() throws Exception {
        StockDto stockDto = new StockDto();
        stockDto.setNationalCode("ABC123");
        stockDto.setEffectiveQuantity(10);
        stockDto.setReservedQuantity(0);
        when(stockService.unReserveQuantityOnDeleteOrCanceled(any(StockOperation.class))).thenReturn(stockDto);

        MvcResult result = mockMvc.perform(delete("/order/lines/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderLines").isEmpty())
                .andReturn();

        OrderDto responseDto = objectMapper.readValue(result.getResponse().getContentAsString(), OrderDto.class);
        assertNotNull(responseDto);
        assertEquals("ORD-CF-123", responseDto.getCode());
        assertTrue(responseDto.getOrderLines().isEmpty());
    }

    @Test
    void testRemoveLine_NotFound() throws Exception {
        mockMvc.perform(delete("/order/lines/999"))
                .andExpect(status().isNotFound());
    }

    // Test per DELETE /order/clearOrder/{code}

    @Test
    void testClearLines_Success() throws Exception {
        StockDto stockDto = new StockDto();
        stockDto.setNationalCode("ABC123");
        stockDto.setEffectiveQuantity(10);
        stockDto.setReservedQuantity(0);
        when(stockService.unReserveQuantityOnDeleteOrCanceled(any(StockOperation.class))).thenReturn(stockDto);

        MvcResult result = mockMvc.perform(delete("/order/clearOrder/ORD-CF-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderLines").isEmpty())
                .andReturn();

        OrderDto responseDto = objectMapper.readValue(result.getResponse().getContentAsString(), OrderDto.class);
        assertNotNull(responseDto);
        assertEquals("ORD-CF-123", responseDto.getCode());
        assertTrue(responseDto.getOrderLines().isEmpty());
    }

    @Test
    void testClearLines_OrderNotFound() throws Exception {
        mockMvc.perform(delete("/order/clearOrder/INVALID"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testClearLines_NonOpenOrder() throws Exception {
        mockMvc.perform(delete("/order/clearOrder/ORD-CF-PICKING"))
                .andExpect(status().isBadRequest());
    }

    // Test per PATCH /order/delete/{code}

    @Test
    void testDeleteOrder_Success() throws Exception {
        StockDto stockDto = new StockDto();
        stockDto.setNationalCode("ABC123");
        stockDto.setEffectiveQuantity(10);
        stockDto.setReservedQuantity(0);
        when(stockService.unReserveQuantityOnDeleteOrCanceled(any(StockOperation.class))).thenReturn(stockDto);

        MvcResult result = mockMvc.perform(patch("/order/delete/ORD-CF-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("CANCELED"))
                .andReturn();

        OrderDto responseDto = objectMapper.readValue(result.getResponse().getContentAsString(), OrderDto.class);
        assertNotNull(responseDto);
        assertEquals("ORD-CF-123", responseDto.getCode());
        assertEquals(State.CANCELED, responseDto.getState());
    }

    @Test
    void testDeleteOrder_NotFound() throws Exception {
        mockMvc.perform(patch("/order/delete/INVALID"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteOrder_NonOpenOrder() throws Exception {
        mockMvc.perform(patch("/order/delete/ORD-CF-PICKING"))
                .andExpect(status().isUnprocessableEntity());
    }

    // Test per GET /order/by-filter

    @Test
    void testGetOrdersByFilter_Success() throws Exception {
        FilterOrdersToRelease filter = new FilterOrdersToRelease(LocalDateTime.now(), "CF123");

        MvcResult result = mockMvc.perform(get("/order/by-filter")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(filter)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].code").value("ORD-CF-123"))
                .andReturn();

        List<OrderDto> responseList = objectMapper.readValue(result.getResponse().getContentAsString(), objectMapper.getTypeFactory().constructCollectionType(List.class, OrderDto.class));
        assertNotNull(responseList);
        assertFalse(responseList.isEmpty());
        assertEquals("ORD-CF-123", responseList.get(0).getCode());
    }

    @Test
    void testGetOrdersByFilter_InvalidCustomer() throws Exception {
        FilterOrdersToRelease filter = new FilterOrdersToRelease(LocalDateTime.now(), "INVALID_CF");

        mockMvc.perform(get("/order/by-filter")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(filter)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetOrdersByFilter_NullFilter() throws Exception {
        mockMvc.perform(get("/order/by-filter")
                .contentType(MediaType.APPLICATION_JSON)
                .content("null"))
                .andExpect(status().isBadRequest());
    }
}