SET FOREIGN_KEY_CHECKS=0;

INSERT INTO shopping_carts (id) VALUES
  (2),
  (3),
  (4);

INSERT INTO cart_items (id, shopping_cart_id, book_id, quantity) VALUES
  (1, 2, 3, 3),
  (2, 3, 1, 1);

SET FOREIGN_KEY_CHECKS=1;
