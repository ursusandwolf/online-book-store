SET FOREIGN_KEY_CHECKS=0;

INSERT INTO orders (id, user_id, status, total, order_date, shipping_address) VALUES
  (101, 2, 'PENDING', 99.99, '2026-01-10 09:00:00', 'Kyiv, Shevchenko ave, 1'),
  (102, 2, 'PENDING', 129.98, '2026-02-10 09:00:00', 'Kyiv, Franka ave, 2'),
  (103, 3, 'COMPLETED', 59.99, '2026-03-10 09:00:00', 'Lviv, Svobody ave, 3'),
  (104, 4, 'DELIVERED', 49.99, '2026-04-10 09:00:00', 'Kharkiv, Sumska ave, 4');

SET FOREIGN_KEY_CHECKS=1;
