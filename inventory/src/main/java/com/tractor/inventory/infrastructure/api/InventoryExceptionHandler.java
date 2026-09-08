package com.tractor.inventory.infrastructure.api;

import com.tractor.common.error.ErrorGroup;
import com.tractor.common.error.TractorNotFoundException;
import com.tractor.inventory.domain.models.InvalidTractorDataException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class InventoryExceptionHandler {

  @ExceptionHandler(TractorNotFoundException.class)
  public ResponseEntity<Map<String, String>> handleTractorNotFound(TractorNotFoundException exception) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body(exception.code(), exception.getMessage()));
  }

  @ExceptionHandler(InvalidTractorDataException.class)
  public ResponseEntity<Map<String, String>> handleInvalidTractorData(InvalidTractorDataException exception) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body(exception.code(), exception.getMessage()));
  }

  @ExceptionHandler(ErrorGroup.class)
  public ResponseEntity<Map<String, Object>> handleErrorGroup(ErrorGroup exception) {
    Map<String, Object> response = Map.of(
        "code", exception.code(),
        "message", exception.getMessage(),
        "errors", exception.getErrors().stream()
                    .map(e -> body(e.code(), e.getMessage()))
                    .toList()
    );

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  private Map<String, String> body(String code, String message) {
    return Map.of("code", code, "message", message);
  }
}
