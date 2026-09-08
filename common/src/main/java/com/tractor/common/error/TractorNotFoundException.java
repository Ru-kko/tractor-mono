package com.tractor.common.error;

import java.util.UUID;

public final class TractorNotFoundException extends TractorStoreException {

  private final UUID tractorId;

  public TractorNotFoundException(UUID tractorId) {
    super("Tractor not found: " + tractorId);
    this.tractorId = tractorId;
  }

  public UUID tractorId() {
    return tractorId;
  }

  @Override
  public String code() {
    return "TRACTOR_NOT_FOUND";
  }
}
