package com.tractor.common.event;

import java.util.UUID;

public record OrderLine(UUID tractorId, int quantity) {
}
