-- Clear existing data
DELETE FROM user_roles;
DELETE FROM users;
DELETE FROM roles;

-- Insert roles
INSERT INTO roles (id, name) VALUES (1, 'ROLE_USER');
INSERT INTO roles (id, name) VALUES (2, 'ROLE_ADMIN');

-- Insert test user
INSERT INTO users (id, username, email, password) 
VALUES (1, 'testuser', 'test@example.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a');

-- Assign roles
INSERT INTO user_roles (user_id, role_id) VALUES (1, 1); 