# Architecture

> This document describes the quality standards and architecture of the project. Reviewer
> agents evaluate the code with respect to these standards. If isn't there, it isn't a
> requirement.

## Principles

1. **Modular Monolith**: The project is a monolith, but it is modular. Each module should be
  self-contained and have a clear interface. Modules should not depend on each other directly.
  Every module must be a gradle subproject with its own `build.gradle` file. These are the modules:
  - `catalog`: Contains the domain model and business logic for querying the catalog of tractors.
  - `inventory`: Contains the domain model and business logic for managing disponibility of
    tractors.
  - `cart`: Contains the domain model and business logic for managing user's shopping cart.
  - `order`: Contains the domain model and business logic for managing orders.
  - `common`: Contains common classes, erros, queries contracts and DTOs used by other modules. It does not contain any domain logic.
  - `monolith`: Contains the main application and the entry point with `spring-modulith`
    configuration.
2. **Module Structure**: Each module must have hexagonal architecture. The structure of each module
  should be as follows:
  ```
  ├── src
  │   ├── main
  │   │   ├── java
  │   │   │   └── com.example.module
  │   │   │       ├── application
  │   │   │       ├── domain
  │   │   │       └── infrastructure
  │   │   └── resources
  │   └── test
  │       └── java
  │           └── com.example.module
  ```
3. **Explicit Exceptions**: All exceptions should be explicit and extend the core
  `TractorStoreException` class. This allows for better error handling and logging.
4. **Dependency Injection**: Every dependency should be injected via constructor injection.
  `@Autowired` is not allowed. This promotes immutability and makes the code easier to test.
5. **Exclusive Table Ownership**: Tables are private to a single module. A table belongs to exactly
  one module, and only that module reads from or writes to it. The `catalog` module must not touch
  the tables of the `inventory` module, and vice versa. Concretely:
  - Every table name is prefixed with the name of the owning module: `catalog_tractor`,
    `inventory_stock_item`, `cart_line`, `order_header`. A table without a module prefix is a
    violation.
  - The entity/mapping classes of a table live only in `infrastructure` of the owning module. No
    other module may declare a mapping over the same table.
  - No foreign keys, joins, views or native queries across tables of different modules. Referential
    integrity between modules is enforced by the application, not by the database.
  - Migration scripts for a table live in the owning module, under
    `<module>/src/main/resources/db/migration/`. A module never migrates another module's schema.
  - Cross-module data is obtained through the other module's public API or through domain events,
    never through its tables. When a module needs a foreign identifier, it stores the plain
    identifier (`tractor_id`) as a value, not as a database relation.
  - The `common` module owns no tables.
