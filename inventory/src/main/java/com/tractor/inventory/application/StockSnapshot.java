package com.tractor.inventory.application;

import lombok.Builder;

import java.util.UUID;

@Builder
public record StockSnapshot(UUID tractorId, Integer stock) {
}
