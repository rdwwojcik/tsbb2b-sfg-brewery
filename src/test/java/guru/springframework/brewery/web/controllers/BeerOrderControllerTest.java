package guru.springframework.brewery.web.controllers;

import guru.springframework.brewery.services.BeerOrderService;
import guru.springframework.brewery.web.model.BeerOrderDto;
import guru.springframework.brewery.web.model.BeerOrderPagedList;
import guru.springframework.brewery.web.model.OrderStatusEnum;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BeerOrderController.class)
class BeerOrderControllerTest {

    @MockBean
    private BeerOrderService beerOrderService;
    @Autowired
    MockMvc mockMvc;
    private BeerOrderDto beerOrderDto;
    private BeerOrderPagedList beerOrderList;

    @BeforeEach
    void setUp() {
        this.beerOrderDto = BeerOrderDto.builder()
                .id(UUID.randomUUID())
                .version(1)
                .createdDate(OffsetDateTime.now())
                .lastModifiedDate(OffsetDateTime.now())
                .customerId(UUID.randomUUID())
                .orderStatus(OrderStatusEnum.NEW)
                .orderStatusCallbackUrl("http://collback.com")
                .customerRef("ref")
                .build();

        List<BeerOrderDto> orders = Arrays.asList(beerOrderDto);
        this.beerOrderList = new BeerOrderPagedList(orders, PageRequest.of(1, 3), 2);
    }

    @AfterEach
    void tearDown() {
        reset(beerOrderService);
    }

    @Test
    void testListOrders() throws Exception {
        when(beerOrderService.listOrders(any(), any())).thenReturn(beerOrderList);

        mockMvc.perform(get("/api/v1/customers/" + beerOrderDto.getCustomerId() + "/orders"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[1].orderStatus", is(OrderStatusEnum.READY.name())));
    }

    @Test
    void testGetOrder() throws Exception {
        String pathGet = "/api/v1/customers/" + beerOrderDto.getCustomerId() + "/orders/" + beerOrderDto.getId();
        when(beerOrderService.getOrderById(any(), any())).thenReturn(beerOrderDto);

        mockMvc.perform(get(pathGet))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.customerRef", is("ref")))
                .andExpect(jsonPath("$.orderStatus", is(OrderStatusEnum.NEW.name())));
    }
}