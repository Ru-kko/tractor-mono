package com.tractor.common.event;

import lombok.Builder;

import java.util.UUID;

@Builder
public record StockUpdatedEvent(UUID tractorId, int stock) {
}
