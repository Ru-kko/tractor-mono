---
id: 003
name: Catalog
sdd: true
state: pending
module: catalog
----
# Catalog Search

This feature will implement the functionality to manage the search services to find tractors.

## Context

The catalog module is responsible for managing the catalog of tractors available in the system.
The search functionality will allow users to find tractors based on various criteria such as, brand
model, year, and other specifications.

## Events

### Incoming Events

- **TractorAdded:** Triggered when a new tractor is added to the inventory, indicating that the 
  catalog should be updated to include the new tractor.
- **TractorOutOfStock:** Triggered when a tractor is out of stock, indicating that the catalog
  should be updated to reflect the unavailability of the tractor. It's should do a safe delete
  of the tractor from the catalog, so it will not be available for search anymore.
- **TractorBackInStock:** Triggered when a tractor is back in stock, indicating that the catalog
  should be updated to reflect the availability of the tractor. It's should do a safe add
  of the tractor to the catalog, so it will be available for search again.

## Queries

- `POST /catalog/search`: Returns a list of tractors based on the search criteria provided in the
  query parameters. The search criteria can include make, model, year, price range, and other 
  specifications.
- `POST /catalog/brand`: Inserts a new brand into the catalog. The request body should contain the
  brand name and any other relevant information.
- `POST /catalog/category`: Inserts a new category into the catalog. The request body should contain
  the category name and any other relevant information.

## Acceptance

- The catalog module must accurately manage the catalog of tractors available in the system.
- The filter criteria for the search must include:
  - `brand`: The brand of the tractor.
  - `model`: The model of the tractor.
  - `year`: The year of manufacture of the tractor.
  - `price`: The price range of the tractor.
  - `horsepower`: The horsepower of the tractor.
  - `weight`: The weight of the tractor.
  - `color`: The color of the tractor.
- The response of search queries must be paginated and include the total number of results, the current page, 
  the number of results per page, and the total number of pages.
- The response must be cursor-based, allowing clients to navigate through the results using cursors instead
  of page numbers.
- The search query must support sorting by various criteria, and ordering the results in ascending or
  descending order based on the specified criteria.
- The ordering criteria for the search must include:
  - `price`: The price of the tractor.
  - `year`: The year of manufacture of the tractor.
  - `horsepower`: The horsepower of the tractor.
  - `weight`: The weight of the tractor.
- Filters must allow operations such as equals, not equals, greater than, less than, and range for numerical 
  fields.
- The catalog module must handle the incoming events to update the catalog accordingly when a tractor is added, 
  goes out of stock, or comes back in stock.