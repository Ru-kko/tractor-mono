package com.tractor.catalog.application.search;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tractor.catalog.domain.models.InvalidCursorException;

import java.util.Base64;
import java.util.UUID;

public final class CatalogCursorCodec {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  public String encode(CatalogCursor cursor) {
    try {
      byte[] json = MAPPER.writeValueAsBytes(
          new CursorPayload(cursor.offset(), cursor.seek(), cursor.lastTractorId()));
      return Base64.getUrlEncoder().withoutPadding().encodeToString(json);
    } catch (Exception exception) {
      throw new InvalidCursorException("Unable to encode cursor");
    }
  }

  public CatalogCursor decode(String cursor) {
    try {
      byte[] json = Base64.getUrlDecoder().decode(cursor);
      CursorPayload payload = MAPPER.readValue(json, CursorPayload.class);
      return new CatalogCursor(payload.seek(), payload.tractorId(), payload.offset());
    } catch (Exception exception) {
      throw new InvalidCursorException("Cursor cannot be decoded: " + cursor);
    }
  }

  private record CursorPayload(int offset, String seek, UUID tractorId) {
  }
}
