-- =============================================================================
-- V2__add_performance_indexes.sql
-- Add missing indexes on foreign keys and status filter columns
-- =============================================================================

CREATE INDEX idx_shop_orders_user_id ON shop_orders (user_id);
CREATE INDEX idx_shop_orders_status ON shop_orders (order_status);
CREATE INDEX idx_order_items_order_id ON order_items (order_id);
CREATE INDEX idx_cart_items_cart_id ON cart_items (cart_id);
CREATE INDEX idx_payments_status ON payments (payment_status);