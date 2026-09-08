package com.tractor.inventory.infrastructure;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tractor.common.error.TractorNotFoundException;
import com.tractor.inventory.application.StockSnapshot;
import com.tractor.inventory.application.TractorSnapshot;
import com.tractor.inventory.domain.models.InvalidTractorDataException;
import com.tractor.inventory.domain.ports.in.InventoryUseCase;
import java.math.BigDecimal;
import java.util.UUID;

import com.tractor.inventory.infrastructure.api.InventoryController;
import com.tractor.inventory.infrastructure.api.InventoryExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(InventoryController.class)
@ContextConfiguration(classes = {InventoryController.class, InventoryExceptionHandler.class})
class InventoryControllerTest {

  private static final UUID TRACTOR_ID = UUID.randomUUID();

  private final MockMvc mockMvc;

  @MockitoBean
  private InventoryUseCase inventoryUseCase;

  InventoryControllerTest(@Autowired MockMvc mockMvc) {
    this.mockMvc = mockMvc;
  }

  @Test
  void getStockReturns200WithStock() throws Exception {
    when(inventoryUseCase.getStock(TRACTOR_ID)).thenReturn(new StockSnapshot(TRACTOR_ID, 15));

    mockMvc.perform(get("/inventory/stock/{tractorId}", TRACTOR_ID))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.tractorId", is(TRACTOR_ID.toString())))
        .andExpect(jsonPath("$.stock", is(15)));
  }

  @Test
  void getStockReturns404WhenUnknown() throws Exception {
    when(inventoryUseCase.getStock(TRACTOR_ID)).thenThrow(new TractorNotFoundException(TRACTOR_ID));

    mockMvc.perform(get("/inventory/stock/{tractorId}", TRACTOR_ID))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("TRACTOR_NOT_FOUND"));
  }

  @Test
  void addTractorReturns201WithCreatedTractor() throws Exception {
    when(inventoryUseCase.addTractor(any()))
        .thenReturn(new TractorSnapshot(TRACTOR_ID, 10, new BigDecimal("15000.00")));

    mockMvc.perform(post("/inventory/tractors")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"stock": 10, "price": 15000.00, "description": "Compact utility tractor",
                 "imageUrl": "https://example.com/t.png", "category": "utility", "brand": "Acme",
                 "model": "X100", "year": 2024, "color": "green", "weight": 1800.5, "horsepower": 75}
                """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.tractorId", is(TRACTOR_ID.toString())))
        .andExpect(jsonPath("$.stock", is(10)));
  }

  @Test
  void addTractorReturns400WhenDataInvalid() throws Exception {
    when(inventoryUseCase.addTractor(any()))
        .thenThrow(new InvalidTractorDataException("price must be greater than zero"));

    mockMvc.perform(post("/inventory/tractors")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"stock": 10, "price": 0, "description": "d", "imageUrl": "i", "category": "c",
                 "brand": "b", "model": "m", "year": 2024, "color": "col", "weight": 100, "horsepower": 50}
                """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("INVALID_TRACTOR_DATA"));
  }

  @Test
  void refillReturns200WithIncreasedStock() throws Exception {
    when(inventoryUseCase.refillStock(TRACTOR_ID, 5)).thenReturn(new StockSnapshot(TRACTOR_ID, 20));

    mockMvc.perform(post("/inventory/tractors/{tractorId}/refill", TRACTOR_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"amount": 5}
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.stock", is(20)));
  }

  @Test
  void refillReturns404WhenUnknown() throws Exception {
    when(inventoryUseCase.refillStock(TRACTOR_ID, 5)).thenThrow(new TractorNotFoundException(TRACTOR_ID));

    mockMvc.perform(post("/inventory/tractors/{tractorId}/refill", TRACTOR_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"amount": 5}
                """))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("TRACTOR_NOT_FOUND"));
  }

  @Test
  void refillReturns400WhenAmountNonPositive() throws Exception {
    when(inventoryUseCase.refillStock(TRACTOR_ID, 0))
        .thenThrow(new InvalidTractorDataException("quantity must be greater than zero"));

    mockMvc.perform(post("/inventory/tractors/{tractorId}/refill", TRACTOR_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"amount": 0}
                """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("INVALID_TRACTOR_DATA"));
  }
}
