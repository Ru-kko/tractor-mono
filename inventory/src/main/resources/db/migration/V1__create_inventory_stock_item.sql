CREATE TABLE inventory_stock_item (
  tractor_id UUID PRIMARY KEY,
  quantity INT NOT NULL,
  price NUMERIC(12,2) NOT NULL,
  version BIGINT NOT NULL DEFAULT 0
);
