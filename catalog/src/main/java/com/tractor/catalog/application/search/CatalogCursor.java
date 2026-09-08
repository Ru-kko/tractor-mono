package com.tractor.catalog.application.search;

import java.util.UUID;

public record CatalogCursor(String seek, UUID lastTractorId, int offset) {
}
