CREATE TABLE catalog_tractor (
  tractor_id UUID PRIMARY KEY,
  brand VARCHAR(255) NOT NULL,
  model VARCHAR(255) NOT NULL,
  year INT NOT NULL,
  price NUMERIC(12,2) NOT NULL,
  horsepower INT NOT NULL,
  weight NUMERIC(12,2) NOT NULL,
  color VARCHAR(255) NOT NULL,
  category VARCHAR(255) NOT NULL,
  description VARCHAR(2000) NOT NULL,
  image_url VARCHAR(2000) NOT NULL,
  stock INT NOT NULL,
  available BOOLEAN NOT NULL DEFAULT TRUE,
  version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_catalog_tractor_available ON catalog_tractor (available);
CREATE INDEX idx_catalog_tractor_price ON catalog_tractor (price);
CREATE INDEX idx_catalog_tractor_year ON catalog_tractor (year);
CREATE INDEX idx_catalog_tractor_horsepower ON catalog_tractor (horsepower);
CREATE INDEX idx_catalog_tractor_weight ON catalog_tractor (weight);
