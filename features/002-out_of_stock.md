---
id: 002
name: Stock Events
sdd: false
state: done
module: inventory
----

# Stock Events

The inventory module must emit events when a tractor is out of stock or when it is back in stock.
This will allow other modules to react accordingly.

## Context

Another modules, cannot know the stock levels of tractors, so they need to know only the availability 
of tractors. The inventory module will emit events when a tractor is out of stock or when it is 
back in stock, allowing other modules to react accordingly.

## Events

### Outbound Events

- **TractorOutOfStock**: Emitted when a tractor is out of stock, allowing other modules to react
  accordingly.
- **TractorBackInStock**: Emitted when a tractor is back in stock, allowing other modules to react
  accordingly.
  