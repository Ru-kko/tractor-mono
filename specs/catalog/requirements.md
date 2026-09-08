# Requirements — `catalog`

**Feature file:** `features/003-catalog.md`
**Owning module:** `catalog`

## Requirements

EARS, one `SHALL` (or `SHALL NOT`) per requirement. Stable ids `R1..R27` —
never renumber once the spec is approved. No `MAY`, `SHOULD`, `CAN`, `MUST`.
Each requirement must be verifiable by one concrete JUnit test.

### Incoming events

R1. WHEN a `TractorAdded` event is received, the catalog module SHALL create a catalog entry for that
tractor with the brand, model, year, price, horsepower, weight, color, category, description, image
URL and stock carried by the event.

R2. WHEN a `TractorOutOfStock` event is received for a tractor id already present in the catalog, the
catalog module SHALL mark that catalog entry as unavailable for search.

R3. WHEN a `TractorBackInStock` event is received for a tractor id already present in the catalog, the
catalog module SHALL mark that catalog entry as available for search.

R4. IF a `TractorOutOfStock` or `TractorBackInStock` event references a tractor id absent from the
catalog, the catalog module SHALL NOT create a catalog entry for it.

R5. The catalog module SHALL exclude catalog entries marked unavailable from the results of
`POST /catalog/search`.

### Search filters

R6. The catalog module SHALL provide a `POST /catalog/search` endpoint that returns the catalog
entries matching the filter criteria in the request body.

R7. WHERE a search request filters on `brand`, `model`, or `color`, the catalog module SHALL support
the `EQUALS` and `NOT_EQUALS` operators for that filter.

R8. WHERE a search request filters on `year`, `price`, `horsepower`, or `weight`, the catalog module
SHALL support the `EQUALS`, `NOT_EQUALS`, `GREATER_THAN`, `LESS_THAN`, and `RANGE` operators for that
filter.

R9. IF a search request specifies an operator not supported for the given filter field, the catalog
module SHALL reject the request with an `InvalidCatalogFilterException`.

R10. IF a search request specifies the `RANGE` operator without both a minimum and a maximum value,
the catalog module SHALL reject the request with an `InvalidCatalogFilterException`.

R11. WHERE a search request specifies more than one filter, the catalog module SHALL return only the
catalog entries that match every filter in the request.

### Sorting

R12. WHERE a search request specifies a sort field, the catalog module SHALL order the results by
`price`, `year`, `horsepower`, or `weight`, whichever is specified.

R13. WHERE a search request specifies a sort direction, the catalog module SHALL order the results in
ascending order or descending order according to the specified direction.

R14. IF a search request specifies a sort field other than `price`, `year`, `horsepower`, or
`weight`, the catalog module SHALL reject the request with an `InvalidCatalogFilterException`.

R15. WHERE a search request specifies no sort field, the catalog module SHALL order the results by
`price` in ascending order.

### Pagination

R16. The catalog module SHALL paginate `POST /catalog/search` responses with a cursor that lets the
client request the next page without repeating catalog entries already returned.

R17. The catalog module SHALL include the total number of matching catalog entries, the current page
number, the number of results per page, and the total number of pages in every `POST /catalog/search`
response.

R18. WHERE a search request specifies a page size, the catalog module SHALL return at most that many
catalog entries in the response.

R19. WHERE a search request specifies no page size, the catalog module SHALL default the page size to
20.

R20. IF a search request specifies a page size lower than 1 or greater than 100, the catalog module
SHALL reject the request with an `InvalidCatalogFilterException`.

R21. IF a search request supplies a cursor value that cannot be decoded, the catalog module SHALL
reject the request with an `InvalidCursorException`.

### Brand management

R22. The catalog module SHALL provide a `POST /catalog/brand` endpoint that creates a brand with the
name given in the request body.

R23. IF a `POST /catalog/brand` request has a blank or missing name, the catalog module SHALL reject
the request with an `InvalidCatalogDataException`.

R24. IF a `POST /catalog/brand` request specifies a name that already exists in the catalog, the
catalog module SHALL reject the request with a `DuplicateBrandException`.

### Category management

R25. The catalog module SHALL provide a `POST /catalog/category` endpoint that creates a category
with the name given in the request body.

R26. IF a `POST /catalog/category` request has a blank or missing name, the catalog module SHALL
reject the request with an `InvalidCatalogDataException`.

R27. IF a `POST /catalog/category` request specifies a name that already exists in the catalog, the
catalog module SHALL reject the request with a `DuplicateCategoryException`.

## Coverage of the acceptance criteria

| Acceptance criterion | Requirements |
|---|---|
| The catalog module must accurately manage the catalog of tractors available in the system. | R1, R2, R3, R4, R5 |
| The filter criteria for the search must include `brand`, `model`, `year`, `price`, `horsepower`, `weight`, `color`. | R6, R7, R8 |
| The response of search queries must be paginated and include the total number of results, the current page, the number of results per page, and the total number of pages. | R17, R18, R19 |
| The response must be cursor-based, allowing clients to navigate through the results using cursors instead of page numbers. | R16, R21 |
| The search query must support sorting by various criteria, and ordering the results in ascending or descending order. | R12, R13, R15 |
| The ordering criteria for the search must include `price`, `year`, `horsepower`, `weight`. | R12, R14 |
| Filters must allow operations such as equals, not equals, greater than, less than, and range for numerical fields. | R7, R8, R9, R10, R11 |
| The catalog module must handle the incoming events to update the catalog accordingly when a tractor is added, goes out of stock, or comes back in stock. | R1, R2, R3, R4 |
| Queries: `POST /catalog/brand` inserts a new brand into the catalog. | R22, R23, R24 |
| Queries: `POST /catalog/category` inserts a new category into the catalog. | R25, R26, R27 |
| Queries: `POST /catalog/search` returns tractors based on search criteria. | R6, R9, R10, R14, R20, R21 |

## Out of scope

The feature file has no explicit `## Out of scope` section; the following is inferred from the
`## Acceptance` and `## Queries` sections so the design does not drift into unspecified behaviour:

- Updating a catalog entry's descriptive fields (price, description, image, brand, category, etc.)
  after creation — no incoming event carries such an update today, only `TractorAdded`,
  `TractorOutOfStock`, and `TractorBackInStock`.
- Hard-deleting a catalog entry — `TractorOutOfStock` is explicitly a safe (soft) delete per the
  feature file.
- Listing, searching, updating, or deleting brands and categories — only creation is in the
  `## Queries` section.
- Authentication/authorization on any of the three endpoints.
- Any endpoint or event other than `POST /catalog/search`, `POST /catalog/brand`,
  `POST /catalog/category`, `TractorAdded`, `TractorOutOfStock`, `TractorBackInStock`.

## Open questions

- `<none>` — the acceptance criteria and events/queries sections are specific enough to write a
  complete set of requirements. Numeric defaults not fixed by the feature file (default page size
  20, maximum page size 100) are design decisions recorded in `design.md`, not open requirements
  questions.
