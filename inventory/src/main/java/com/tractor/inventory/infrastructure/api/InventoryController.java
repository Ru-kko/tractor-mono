package com.tractor.inventory.infrastructure.api;

import com.tractor.inventory.application.*;
import com.tractor.inventory.domain.ports.in.InventoryUseCase;
import com.tractor.inventory.infrastructure.api.dto.AddTractorRequest;
import com.tractor.inventory.infrastructure.api.dto.RefillStockRequest;
import com.tractor.inventory.infrastructure.api.dto.StockResponse;
import com.tractor.inventory.infrastructure.api.dto.TractorCreatedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/inventory")
public class InventoryController {
  private final InventoryUseCase inventoryService;

  @Operation(summary = "Get the current stock quantity for a tractor")
  @ApiResponse(responseCode = "200", description = "Stock found")
  @ApiResponse(responseCode = "404", description = "Tractor not found")
  @GetMapping("/stock/{tractorId}")
  public ResponseEntity<StockResponse> getStock(@PathVariable UUID tractorId) {
    StockSnapshot snapshot = inventoryService.getStock(tractorId);
    return ResponseEntity.ok(new StockResponse(snapshot.tractorId(), snapshot.stock()));
  }

  @Operation(summary = "Add a new tractor to the inventory")
  @ApiResponse(responseCode = "201", description = "Tractor created")
  @ApiResponse(responseCode = "400", description = "Invalid tractor data")
  @PostMapping("/tractors")
  public ResponseEntity<TractorCreatedResponse> addTractor(@RequestBody AddTractorRequest request) {
    TractorSnapshot snapshot = inventoryService.addTractor(new AddTractorCommand(
        request.stock(), request.price(), request.description(), request.imageUrl(),
        request.category(), request.brand(), request.model(), request.year(), request.color(),
        request.weight(), request.horsepower()));

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(new TractorCreatedResponse(snapshot.tractorId(), snapshot.stock(), snapshot.price()));
  }

  @Operation(summary = "Refill the stock quantity for a tractor")
  @ApiResponse(responseCode = "200", description = "Stock refilled")
  @ApiResponse(responseCode = "404", description = "Tractor not found")
  @ApiResponse(responseCode = "400", description = "Invalid refill amount")
  @PostMapping("/tractors/{tractorId}/refill")
  public ResponseEntity<StockResponse> refill(
      @PathVariable UUID tractorId, @RequestBody RefillStockRequest request) {
    StockSnapshot snapshot = inventoryService.refillStock(tractorId, request.amount());
    return ResponseEntity.ok(new StockResponse(snapshot.tractorId(), snapshot.stock()));
  }
}
