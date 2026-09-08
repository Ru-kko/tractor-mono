package com.tractor.common.event;

import lombok.Builder;

import java.util.UUID;

@Builder
public record TractorBackInStockEvent(UUID tractorId) {
}
