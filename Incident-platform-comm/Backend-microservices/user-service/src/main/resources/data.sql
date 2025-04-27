-- This file will automatically be executed by Spring Boot when the application starts
-- if spring.sql.init.mode=always is set in application.properties

-- Ensure roles are created (these will also be created programmatically in UserServiceApplication.java)
INSERT IGNORE INTO roles(name) VALUES('L1_SUPPORT');
INSERT IGNORE INTO roles(name) VALUES('L2_SUPPORT');
INSERT IGNORE INTO roles(name) VALUES('MANAGER');
INSERT IGNORE INTO roles(name) VALUES('ADMIN');

-- Create a default admin user (password: admin123)
-- Note: In a production environment, never store plaintext passwords. This is just for development.
-- INSERT INTO users (username, email, password, first_name, last_name, active)
-- VALUES ('admin', 'admin@example.com', '$2a$10$ywmVt7vqRRO5lKFDOcEkDeYFgYVIKO3J23XBtFP5ou4QGgI1xwV1y', 'Admin', 'User', true);

-- Assign admin role to admin user
-- INSERT INTO user_roles (user_id, role_id) VALUES (1, 5);