package com.tractor.inventory.infrastructure.api.dto;

import java.util.UUID;

public record StockResponse(UUID tractorId, int stock) {
}
