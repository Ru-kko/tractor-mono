package com.tractor.common.error;

import lombok.Getter;

import java.util.List;

@Getter
public class ErrorGroup extends TractorStoreException {
  private final List<TractorStoreException> errors;

  public ErrorGroup(String message, List<? extends TractorStoreException> errors) {
    super(message);
    this.errors = List.copyOf(errors);
  }

  @Override
  public String code() {
    return "MANY_ERRORS";
  }
}
