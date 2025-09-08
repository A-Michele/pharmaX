INSERT INTO contact (email, phone_number)
VALUES ('test@example.com', '1234567890');

INSERT INTO customer (cf, name, shipping_address, billing_address, contacts_id)
VALUES ('CF123', 'Test Customer', 'Via Test 123', 'Via Test 456', (SELECT id FROM contact WHERE email = 'test@example.com'));

INSERT INTO product (national_code, name, category, supplier_name)
VALUES ('ABC123', 'Product A', 'CATEGORY_A', 'Supplier A');

INSERT INTO product (national_code, name, category, supplier_name)
VALUES ('ABD123', 'Product B', 'CATEGORY_B', 'Supplier B');

INSERT INTO product (national_code, name, category, supplier_name)
VALUES ('XYZ123', 'Product C', 'CATEGORY_C', 'Supplier C');

INSERT INTO customer_order (code, state, cf, date)
VALUES ('ORD-CF-123', 'OPEN', 'CF123', CURRENT_TIMESTAMP);

INSERT INTO customer_order (code, state, cf, date)
VALUES ('ORD-CF-NOP', 'CANCELED', 'CF123', CURRENT_TIMESTAMP);

INSERT INTO customer_order (code, state, cf, date)
VALUES ('ORD-CF-PICKING', 'PICKING', 'CF123', CURRENT_TIMESTAMP);

INSERT INTO customer_order (code, state, cf, date)
VALUES ('ORD-CF-999', 'OPEN', 'CF123', CURRENT_TIMESTAMP);

INSERT INTO customer_order (code, state, cf, date)
VALUES ('ORD-CF-INVALID-CF', 'OPEN', 'INVALID_CF', CURRENT_TIMESTAMP);

INSERT INTO customer_order (code, state, cf, date)
VALUES ('ORD-NC-INVALID', 'OPEN', 'CF123', CURRENT_TIMESTAMP);

INSERT INTO order_line (national_code, quantity, line_number, type, order_id)
VALUES ('ABC123', 5, 'ORDLINE-123456', 'OPEN', (SELECT id FROM customer_order WHERE code = 'ORD-CF-123'));

INSERT INTO order_line (national_code, quantity, line_number, type, order_id)
VALUES ('ABC123', 5, 'ORDLINE-123465', 'CANCELED', (SELECT id FROM customer_order WHERE code = 'ORD-CF-NOP'));

INSERT INTO order_line (national_code, quantity, line_number, type, order_id)
VALUES ('ABC123', 5, 'ORDLINE-NOORDER', 'CANCELED', null);

INSERT INTO order_line (national_code, quantity, line_number, type, order_id)
VALUES ('ABC123', 5, 'ORDLINE-NOT-EXISTING-ORDER', 'CANCELED', (SELECT id FROM customer_order WHERE code = 'ORD-CF-NOT-EXISTING'));

INSERT INTO order_line (national_code, quantity, line_number, type, order_id)
VALUES ('ABC123', 5, 'ORDLINE-ORD-NO-OPEN', 'OPEN', (SELECT id FROM customer_order WHERE code = 'ORD-CF-PICKING'));

INSERT INTO order_line (national_code, quantity, line_number, type, order_id)
VALUES ('INVALID-NC', 5, 'ORDLINE-INVALID-NC', 'OPEN', (SELECT id FROM customer_order WHERE code = 'ORD-NC-INVALID'));