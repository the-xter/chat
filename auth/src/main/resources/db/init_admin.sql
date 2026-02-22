-- Insert default admin user
-- Password must be BCrypt encoded. Use https://bcrypt-generator.com/ to generate one.
-- The value below corresponds to: changeme
INSERT INTO users (username, email, password)
VALUES ('admin', 'admin@example.com', '$2a$12$pqE1xoGEbMQMVZURmuFtFuvCAl3RjPMDUDGFP1rHHPPZUq6ICf8Pu');

INSERT INTO user_roles (user_id, role)
SELECT id, 'ADMIN' FROM users WHERE username = 'admin';
