package com.tractor.inventory.application;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record TractorSnapshot(UUID tractorId, int stock, BigDecimal price) {
}
