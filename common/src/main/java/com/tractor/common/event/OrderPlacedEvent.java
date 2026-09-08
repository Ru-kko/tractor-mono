package com.tractor.common.event;

import java.util.List;
import java.util.UUID;

public record OrderPlacedEvent(UUID orderId, List<OrderLine> lines) {
}
