-- Migration V2: Add user ownership column to basket_items and orders tables
ALTER TABLE basket_items ADD COLUMN user_id BIGINT;
ALTER TABLE orders ADD COLUMN user_id BIGINT;

CREATE INDEX idx_basket_items_user_id ON basket_items(user_id);
CREATE INDEX idx_orders_user_id ON orders(user_id);
