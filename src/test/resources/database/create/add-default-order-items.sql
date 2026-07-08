SET FOREIGN_KEY_CHECKS=0;

INSERT INTO order_items (id, order_id, book_id, quantity, price) VALUES
  (1001, 101, 1, 2, 79.98),
  (1002, 101, 2, 1, 54.99),
  (1003, 104, 3, 1, 49.99);

SET FOREIGN_KEY_CHECKS=1;
