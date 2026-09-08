CREATE TABLE catalog_category (
  id UUID PRIMARY KEY,
  name VARCHAR(255) NOT NULL
);

CREATE UNIQUE INDEX idx_catalog_category_name ON catalog_category (lower(name));
