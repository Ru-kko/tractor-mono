---
id: 001
name: inventory
sdd: true
state: done
module: inventory
---

# Inventory management

This feature will implement the functionality for managing the inventory of tractors in the system.

## Context

The inventory module is responsible for tracking the availability of tractors. It will handle stock levels, reservations, and updates to the inventory based on orders and returns.

## Events

### Inbound Events

- **OrderPlaced**: Triggered when an order is placed, indicating that the inventory should be updated to reflect the reserved stock.

### Outbound Events

- **StockUpdated**: Emitted when the stock levels are updated, allowing other modules to react accordingly.
- **TractorAdded**: Emitted when a new tractor is added to the inventory, allowing other modules to be aware of the new product.


## Queries

- `/inventory/stock/{tractorId}`: Returns the current stock level for a specific tractor.

## Commands

- `POST /inventory/tractors/{tractorId}/refill`: Refills the stock for a specific tractor.
- `POST /inventory/tractors/{tractorId}/new`: Adds a new tractor to the inventory with an initial stock level.

## Acceptance

- The inventory module must accurately track stock levels for each tractor.
- The module must expose endpoints for querying stock levels and updating inventory.
- The module must expose a query interface for other modules to check stock availability.
- The module must validate the following data to create a new tractor in the inventory:
  - `stock`: Initial stock level for the tractor.
  - `price`: Price of the tractor.
  - `description`: Description of the tractor.
  - `imageUrl`: URL of the tractor's image.
  - `category`: Category of the tractor.
  - `brand`: Brand of the tractor.
  - `model`: Model of the tractor.
  - `year`: Year of manufacture of the tractor.
  - `color`: Color of the tractor.
  - `weight`: Weight of the tractor.
  - `horsepower`: Horsepower of the tractor.
- The module must save the following data to create a new tractor in the inventory:
  - `tractorId`: Unique identifier for the tractor.
  - `stock`: Initial stock level for the tractor.
  - `price`: Price of the tractor.
- The module must emit the `TractorAdded` event when a new tractor is added to the inventory.
- The module must emit the `StockUpdated` event when the stock levels are updated.
- The module must handle the `OrderPlaced` event to update the stock levels accordingly.
- The module must ensure that stock levels do not go below zero when processing orders.
- When tractors are added, the module must ensure that all required fields are provided and valid.
- When tractors are added, the module must create a new UUID for the `tractorId`, save it and emit the `TractorAdded` event with the new `tractorId`.