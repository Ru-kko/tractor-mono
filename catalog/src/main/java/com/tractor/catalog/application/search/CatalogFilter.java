package com.tractor.catalog.application.search;

public record CatalogFilter(FilterField field, FilterOperator operator, String value, String min, String max) {
}
