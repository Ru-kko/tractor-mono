CREATE TABLE catalog_brand (
  id UUID PRIMARY KEY,
  name VARCHAR(255) NOT NULL
);

CREATE UNIQUE INDEX idx_catalog_brand_name ON catalog_brand (lower(name));
