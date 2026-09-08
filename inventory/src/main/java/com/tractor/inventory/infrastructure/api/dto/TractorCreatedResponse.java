package com.tractor.inventory.infrastructure.api.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record TractorCreatedResponse(UUID tractorId, int stock, BigDecimal price) {
}
