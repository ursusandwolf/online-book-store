INSERT INTO users (id, email, password, first_name, last_name, is_deleted)
VALUES (1, 'admin@test.com', 'hashed_password', 'Admin', 'User', FALSE);

INSERT INTO roles (id, name) VALUES (1, 'ADMIN'), (2, 'USER');
INSERT INTO user_roles (user_id, role_id) VALUES (1, 1);