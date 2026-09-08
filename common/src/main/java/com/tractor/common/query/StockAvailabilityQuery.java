package com.tractor.common.query;

import java.util.Optional;
import java.util.UUID;

public interface StockAvailabilityQuery {

  Optional<Integer> currentStock(UUID tractorId);
}
