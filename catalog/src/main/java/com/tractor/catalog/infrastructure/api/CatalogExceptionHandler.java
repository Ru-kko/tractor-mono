package com.tractor.catalog.infrastructure.api;

import com.tractor.catalog.domain.models.DuplicateBrandException;
import com.tractor.catalog.domain.models.DuplicateCategoryException;
import com.tractor.catalog.domain.models.InvalidCatalogDataException;
import com.tractor.catalog.domain.models.InvalidCatalogFilterException;
import com.tractor.catalog.domain.models.InvalidCursorException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CatalogExceptionHandler {

  @ExceptionHandler(InvalidCatalogFilterException.class)
  public ResponseEntity<Map<String, String>> handleInvalidCatalogFilter(InvalidCatalogFilterException exception) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body(exception.code(), exception.getMessage()));
  }

  @ExceptionHandler(InvalidCursorException.class)
  public ResponseEntity<Map<String, String>> handleInvalidCursor(InvalidCursorException exception) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body(exception.code(), exception.getMessage()));
  }

  @ExceptionHandler(InvalidCatalogDataException.class)
  public ResponseEntity<Map<String, String>> handleInvalidCatalogData(InvalidCatalogDataException exception) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body(exception.code(), exception.getMessage()));
  }

  @ExceptionHandler(DuplicateBrandException.class)
  public ResponseEntity<Map<String, String>> handleDuplicateBrand(DuplicateBrandException exception) {
    return ResponseEntity.status(HttpStatus.CONFLICT).body(body(exception.code(), exception.getMessage()));
  }

  @ExceptionHandler(DuplicateCategoryException.class)
  public ResponseEntity<Map<String, String>> handleDuplicateCategory(DuplicateCategoryException exception) {
    return ResponseEntity.status(HttpStatus.CONFLICT).body(body(exception.code(), exception.getMessage()));
  }

  private Map<String, String> body(String code, String message) {
    return Map.of("code", code, "message", message);
  }
}
